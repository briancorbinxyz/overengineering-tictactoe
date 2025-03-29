package org.xxdc.oss.example.transport.tcp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.Socket;
import org.xxdc.oss.example.GameState;
import org.xxdc.oss.example.transport.*;

/**
 * A {@link TransportServer} implementation that uses TCP sockets to handle communication between a
 * client and server. This class is responsible for initializing the socket connection, sending game
 * state updates to the client, and receiving client input through the socket.
 */
public class TcpTransportServer implements TransportServer {

  private static final Logger log = System.getLogger(TcpTransportServer.class.getName());

  private final Socket socket;

  private final MessageHandler handler;

  /**
   * Constructs a new {@link TcpTransportServer} instance with the provided {@link Socket}. This
   * constructor initializes the {@link SecureBouncyCastleKyberServer} with a {@link
   * DuplexMessageHandler} that uses the input and output streams of the provided socket.
   *
   * @param socket the {@link Socket} to use for the transport server
   * @throws TransportException if an {@link IOException} occurs while initializing the message
   *     handler
   */
  public TcpTransportServer(Socket socket) {
    this.socket = socket;
    try {
      this.handler =
          new SecureKyberServer(
              new DuplexMessageHandler(
                  new ObjectOutputStream(socket.getOutputStream()),
                  new ObjectInputStream(socket.getInputStream())));
    } catch (IOException e) {
      throw new TransportException("IO exception: " + e.getMessage(), e);
    }
  }

  /**
   * Constructs a new {@link TcpTransportServer} instance with the provided {@link Socket} and
   * {@link MessageHandler}. This constructor initializes the {@link TcpTransportServer} with the
   * given socket and message handler.
   *
   * @param socket the {@link Socket} to use for the transport server
   * @param handler the {@link MessageHandler} to use for the transport server
   */
  public TcpTransportServer(Socket socket, MessageHandler handler) {
    this.socket = socket;
    this.handler = handler;
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
