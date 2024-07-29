package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Scanner;

/**
 * Represents a human player in the game. The human player interacts with the game by providing
 * their next move through the console.
 */
public record HumanPlayer(String playerMarker) implements Player, Serializable {

    private static final Logger log = System.getLogger(HumanPlayer.class.getName());

    private static final long serialVersionUID = 1L;

    @Override
    public String getPlayerMarker() {
        return playerMarker;
    }

    @Override
    public int nextMove(GameBoard board) {
        int location;
        var io = System.console() != null ? new ConsoleInput() : new ScannerInput();
        do {
            System.out.print(
                    "Player '"
                            + playerMarker
                            + "' choose an available location between [0-"
                            + (board.getDimension() * board.getDimension() - 1)
                            + "]: ");
            try {
                var msg = io.readLine();
                location = Integer.parseInt(msg);
            } catch (NumberFormatException e) {
                // expected if user enters non-integer carry on.
                log.log(Level.TRACE, "Invalid location: " + e.getMessage(), e);
                location = -1;
            }
        } while (!board.isValidMove(location));
        return location;
    }

        static sealed interface Input {
        String readLine();
    }

    static final class ConsoleInput implements Input {
        @Override
        public String readLine() {
            return System.console().readLine();
        }
    }

    static final class ScannerInput implements Input {
        @Override
        public String readLine() {
            try (Scanner scanner = new Scanner(new CloseOnExitInputStream(System.in))) {
                return scanner.nextLine();
            }
        }
    
    }

    static class CloseOnExitInputStream extends InputStream {

        private final InputStream in;

        public CloseOnExitInputStream(InputStream in) {
            this.in = in;
        }

        @Override
        public void close() {
            // no op
        }

		@Override
		public int read() throws IOException {
            return in.read();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return in.read(b, off, len);
        }

        @Override
        public int read(byte[] b) throws IOException {
            return in.read(b);
        }

    }
}
