package org.xxdc.oss.example.transport.tcp;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.invoke.MethodHandles;
import org.xxdc.oss.example.GameState;
import org.xxdc.oss.example.MessageHandler;
import org.xxdc.oss.example.Player;
import org.xxdc.oss.example.transport.TransportException;

/**
 * Represents a TCP transport client that handles communication with a server. The client is
 * responsible for initializing the connection, sending and receiving messages, and handling the
 * game logic based on the received messages.
 *
 * @param <T> the type of player that the client represents
 * @param connection the message handler used to communicate with the server
 * @param player the player instance
 */
public record TcpTransportClient<T extends Player>(MessageHandler connection, T player)
    implements AutoCloseable {

  private static final Logger log =
      System.getLogger(MethodHandles.lookup().lookupClass().getName());

  /**
   * Initializes a new instance of the {@link TcpTransportClient} class with the specified {@link
   * MessageHandler} connection and {@link Player} instance.
   *
   * @param connection the message handler used to communicate with the server
   * @param player the player instance
   * @throws TransportException if an I/O exception occurs during initialization
   */
  public TcpTransportClient(MessageHandler connection, T player) {
    try {
      this.connection = connection;
      this.player = player;
      this.connection.init();
    } catch (IOException e) {
      throw new TransportException(e);
    }
  }

  /**
   * Runs the TCP transport client, initializing the player marker, receiving messages from the
   * server, and handling the game logic based on the received messages. The client will continue to
   * receive messages until an exit code is received from the server.
   */
  public void run() {
    log.log(Level.DEBUG, "Started TCP transport client");
    try {
      String playerMarker = initPlayerMarker();
      log.log(Level.DEBUG, "Playing as {0}", playerMarker);

      String msg;
      while ((msg = connection.receiveMessage()) != null && !msg.equals(TcpProtocol.EXIT_CODE)) {
        log.log(Level.DEBUG, "Received message from server: {0} for {1}", msg, player);
        TcpProtocol.fromNextMoveState(msg)
            .ifPresentOrElse(
                (state) -> makeMove(state),
                () -> {
                  log.log(Level.ERROR, "Invalid message from transport");
                  throw new TransportException("Invalid message from transport");
                });
      }
      handleExit(msg);
    } catch (IOException e) {
      throw new TransportException("IO exception: " + e.getMessage(), e);
    }
  }

  private void makeMove(GameState state) {
    int nextMove = player.nextMove(state);
    try {
      connection.sendMessage(String.valueOf(nextMove));
    } catch (IOException e) {
      throw new TransportException("IO exception: " + e.getMessage(), e);
    }
  }

  @Override
  public void close() throws Exception {
    connection.close();
  }

  private void handleExit(String serverMessage) {
    if (serverMessage == null) {
      log.log(Level.DEBUG, "Received null message from server");
    } else {
      log.log(Level.DEBUG, "Received exit code from server");
    }
  }

  private String initPlayerMarker() throws IOException {
    String serverMessage = connection.receiveMessage();
    log.log(Level.DEBUG, "Received initial message from server: {0}", serverMessage);
    String playerMarker =
        TcpProtocol.fromGameStartedState(serverMessage)
            .orElseThrow(
                () ->
                    new TransportException(
                        "Invalid server message received. Expected game" + " started state."));
    log.log(Level.DEBUG, "Received assigned player marker: {0}", playerMarker);
    return playerMarker;
  }
}
