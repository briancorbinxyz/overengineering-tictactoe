package org.example;

import java.io.Serializable;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.function.ToIntFunction;
import org.example.transport.TransportConfiguration;
import org.example.transport.TransportServer;

public sealed interface PlayerNode extends ToIntFunction<GameBoard> {

    /**
     * Gets the player's marker, which is a string representation of the player's identity.
     *
     * @return the player's marker
     */
    String playerMarker();

    /**
     * Gets the player's next move to apply to the given game board.
     *
     * @param board the game board to apply the move to
     * @return the location on the game board where the move was made
     */
    public int applyAsInt(GameBoard board);

    public record Local<P extends Player>(P player) implements PlayerNode, Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * Gets the player's next move to apply to the given game board.
         *
         * @param board the game board to apply the move to
         * @return the location on the game board where the move was made
         */
        @Override
        public int applyAsInt(GameBoard board) {
            return player.nextMove(board);
        }

        @Override
        public String playerMarker() {
            return player.playerMarker();
        }
    }

    public record Remote(String playerMarker, TransportServer transport)
            implements PlayerNode, AutoCloseable {

        private static final Logger log = System.getLogger(Remote.class.getName());

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
        public int applyAsInt(GameBoard board) {
            int location = -1;
            do {
                try {
                    transport.send(board);
                    // After receiving the game board the player should send a move
                    location = transport.accept(board);
                } catch (NumberFormatException e) {
                    log.log(Level.TRACE, "Invalid move from client: {0}", e.getMessage(), e);
                }
            } while (!board.isValidMove(location));
            return location;
        }

        @Override
        public void close() throws Exception {
            transport.close();
        }

        @Override
        public String playerMarker() {
            return playerMarker;
        }
    }
}
