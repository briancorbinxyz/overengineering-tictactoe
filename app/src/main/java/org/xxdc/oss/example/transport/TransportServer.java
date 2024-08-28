package org.xxdc.oss.example.transport;

import org.xxdc.oss.example.GameState;

/** A server that communicates with a client over a transport protocol. */
public interface TransportServer extends AutoCloseable {

  /**
   * Initialize the transport server.
   *
   * @param config the configuration for the transport server
   */
  public void initialize(TransportConfiguration config);

  /**
   * Send the given game state to the client.
   *
   * @param state the game state to send
   */
  public void send(GameState state);

  /**
   * Receive a move selection from the client.
   *
   * @return the move selection
   */
  public int accept();
}
