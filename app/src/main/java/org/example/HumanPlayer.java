package org.example;

import java.io.Serializable;

/**
 * Represents a human player in the game.
 * The human player interacts with the game by providing their next move through the console.
 */
public record HumanPlayer(String playerMarker) implements Player, Serializable {

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
            System.out.print("Player '" + playerMarker + "' choose an available location between [0-" + (board.getDimension()*board.getDimension()-1) + "]: ");
            location = Integer.parseInt(io.readLine());
        } while (!board.isValidMove(location));
        return location;
    }
}
