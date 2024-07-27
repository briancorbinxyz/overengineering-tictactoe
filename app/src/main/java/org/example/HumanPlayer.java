package org.example;

import java.io.Serializable;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

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
        var io = System.console();
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
                log.log(Level.TRACE, "Invalid location: ", e.getMessage(), e);
                location = -1;
            }
        } while (!board.isValidMove(location));
        return location;
    }
}
