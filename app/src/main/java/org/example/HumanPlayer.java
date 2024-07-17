package org.example;

import java.io.Serializable;
import java.util.Scanner;

/**
 * Represents a human player in the game.
 * The human player interacts with the game by providing their next move through the console.
 */
public final class HumanPlayer implements Player, Serializable {

    private final String playerMarker;

    public HumanPlayer(String playerMarker) {
        this.playerMarker = playerMarker;
    }

    @Override
    public String getPlayerMarker() {
        return playerMarker;
    }

    @Override
    public int nextMove(GameBoard board) {
        int location;
        Scanner io = new Scanner(System.in);
        do {
            System.out.print("Player '" + playerMarker + "' choose an available location between [0-" + (board.getDimension()*board.getDimension()-1) + "]: ");
            location = io.nextInt();
        } while (!board.isValidMove(location));
        return location;
    }
}
