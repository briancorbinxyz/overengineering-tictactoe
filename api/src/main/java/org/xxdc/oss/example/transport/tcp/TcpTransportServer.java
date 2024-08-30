package org.xxdc.oss.example.transport.tcp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.Socket;
import org.xxdc.oss.example.GameState;
import org.xxdc.oss.example.MessageHandler;
import org.xxdc.oss.example.RemoteMessageHandler;
import org.xxdc.oss.example.SecureMessageHandler;
import org.xxdc.oss.example.transport.TransportConfiguration;
import org.xxdc.oss.example.transport.TransportException;
import org.xxdc.oss.example.transport.TransportServer;

/** A {@link TransportServer} implementation that uses TCP sockets. */
public class TcpTransportServer implements TransportServer {

  private static final Logger log = System.getLogger(TcpTransportServer.class.getName());

  private final Socket socket;

  private transient MessageHandler handler;

  public TcpTransportServer(Socket socket) {
    this.socket = socket;
    try {
      this.handler =
          new SecureMessageHandler.Server(
              new RemoteMessageHandler(
                  new ObjectOutputStream(socket.getOutputStream()),
                  new ObjectInputStream(socket.getInputStream())));
    } catch (IOException e) {
      throw new TransportException("IO exception: " + e.getMessage(), e);
    }
  }

  @Override
  public void initialize(TransportConfiguration configuration) {
    log.log(
        Level.DEBUG,
        "Initializing socket {0} for {1} to client for Tic-Tac-Toe.",
        socket,
        configuration.playerMarker());
    try {
      handler.init();
      handler.sendMessage(
          String.format(TcpProtocol.GAME_STARTED_JSON_FORMAT, configuration.playerMarker()));
    } catch (IOException e) {
      log.log(
          Level.WARNING,
          "Error initializing socket {0} for {1} to client for Tic-Tac-Toe.",
          socket,
          configuration.playerMarker());
      throw new TransportException(e.getMessage(), e);
    }
  }

  @Override
  public void close() throws Exception {
    if (socket.isClosed()) {
      return;
    }
    handler.sendMessage(TcpProtocol.EXIT_CODE);
    handler.close();
  }

  @Override
  public void send(GameState state) {
    try {
      var nextMoveMsg = String.format(TcpProtocol.NEXT_MOVE_JSON_FORMAT, state.asJsonString());
      log.log(Level.DEBUG, "Sending message to client: {0}", nextMoveMsg);
      handler.sendMessage(nextMoveMsg);
    } catch (IOException e) {
      throw new TransportException(e.getMessage(), e);
    }
  }

  @Override
  public int accept() {
    try {
      var clientMessage = handler.receiveMessage();
      return Integer.parseInt(clientMessage);
    } catch (IOException e) {
      throw new TransportException(e.getMessage(), e);
    }
  }
}
