package org.xxdc.oss.example;

import java.io.Serializable;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.function.ToIntFunction;
import org.xxdc.oss.example.transport.TransportConfiguration;
import org.xxdc.oss.example.transport.TransportServer;

/// The `PlayerNode` interface represents a player in a game. It provides methods to get the
// player's
/// marker and apply the player's next move to the game state.
///
/// The `Local` implementation of `PlayerNode` represents a local player, where the player's logic
/// is implemented directly in the application.
///
/// The `Remote` implementation of `PlayerNode` represents a remote player, where the player's
/// logic is implemented in a separate process and communicated with the application through a
/// transport mechanism.
public sealed interface PlayerNode extends ToIntFunction<GameState> {

  /**
   * Gets the player's marker: a string representation of the player's identity. Returns the marker
   * (e.g. "X" or "O") used by this player.
   *
   * @return the player's marker
   */
  String playerMarker();

  /**
   * Gets the player's next move to apply to the game board with the current game state.
   *
   * @param state the current state of the game
   * @return the location on the game board where the move was made
   */
  public int applyAsInt(GameState state);

  /**
   * Represents a local player node that implements the player's logic directly in the application.
   * The local player node is responsible for providing the player's next move to apply to the game
   * board. The local player node is serializable, allowing it to be persisted and transferred
   * between processes.
   *
   * @param <P> the type of the player implementation
   */
  public final class Local<P extends Player> implements PlayerNode, Serializable {

    private static final long serialVersionUID = 1L;

    private final P player;

    private final String playerMarker;

    /**
     * Constructs a new Local PlayerNode instance with the given player marker and player
     * implementation.
     *
     * @param playerMarker the marker to identify this player
     * @param player the player implementation to use for this node
     */
    public Local(String playerMarker, P player) {
      this.playerMarker = playerMarker;
      this.player = player;
    }

    /**
     * Gets the player's next move to apply to the given game board.
     *
     * @param state the current state of the game
     * @return the location on the game board where the move was made
     */
    @Override
    public int applyAsInt(GameState state) {
      return player.nextMove(state);
    }

    @Override
    public String playerMarker() {
      return playerMarker;
    }

    public Player player() {
      return player;
    }

    @Override
    public String toString() {
      return "Local{" + "playerMarker=" + playerMarker + ", player=" + player + "}";
    }
  }

  /**
   * Represents a remote player node that communicates with a client over a transport server. The
   * remote player node sends the current game state to the client and receives the player's next
   * move. The remote player node is responsible for validating the received move and applying it to
   * the game board. The remote player node must be closed when the game is finished to release the
   * transport server resources.
   */
  public final class Remote implements PlayerNode, AutoCloseable {

    private static final Logger log = System.getLogger(Remote.class.getName());

    private String playerMarker;

    private final TransportServer transport;

    /**
     * Constructs a new Remote PlayerNode instance that communicates with a client over the provided
     * TransportServer.
     *
     * @param playerMarker the marker to identify this player
     * @param transport the TransportServer to use for communication with the client
     * @throws IllegalArgumentException if the provided TransportServer is null
     */
    public Remote(String playerMarker, TransportServer transport) {
      this.playerMarker = playerMarker;
      this.transport = transport;
      if (transport == null) {
        throw new IllegalArgumentException("TransportServer cannot be null");
      }
      transport.initialize(new TransportConfiguration(playerMarker));
    }

    /**
     * Gets the player's next move to apply to the given game board.
     *
     * @param state the current state of the game
     * @return the location on the game board where the move was made
     */
    @Override
    public int applyAsInt(GameState state) {
      int move = -1;
      do {
        try {
          log.log(Level.DEBUG, "Sending game state to client: {0}", state);
          transport.send(state);
          // After receiving the game state the player should send a move
          move = transport.accept();
          if (log.isLoggable(Level.DEBUG)) {
            log.log(Level.DEBUG, "Received move from client: {0}", move);
            log.log(Level.DEBUG, "Move is valid: {0}", state.board().isValidMove(move));
          }
        } catch (NumberFormatException e) {
          log.log(Level.TRACE, "Invalid move from client: {0}", e.getMessage(), e);
        }
      } while (!state.board().isValidMove(move));
      return move;
    }

    @Override
    public void close() throws Exception {
      transport.close();
    }

    @Override
    public String playerMarker() {
      return playerMarker;
    }

    @Override
    public String toString() {
      return "Remote{"
          + "playerMarker="
          + playerMarker
          + ", transport="
          + transport.getClass().getSimpleName()
          + '}';
    }
  }
}
