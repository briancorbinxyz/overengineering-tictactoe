package org.xxdc.oss.example;

import java.io.Serializable;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.function.ToIntFunction;

import org.xxdc.oss.example.transport.TransportConfiguration;
import org.xxdc.oss.example.transport.TransportServer;

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
   * @param board to apply the move to
   * @return the location on the game board where the move was made
   */
  public int applyAsInt(GameState state);

  public final class Local<P extends Player> implements PlayerNode, Serializable {

    private static final long serialVersionUID = 1L;

    private final P player;

    private final String playerMarker;

    public Local(String playerMarker, P player) {
      this.playerMarker = playerMarker;
      this.player = player;
    }

    /**
     * Gets the player's next move to apply to the given game board.
     *
     * @param board the game board to apply the move to
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

  public final class Remote implements PlayerNode, AutoCloseable {

    private static final Logger log = System.getLogger(Remote.class.getName());

    private String playerMarker;

    private final TransportServer transport;

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
     * @param board the game board to apply the move to
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
