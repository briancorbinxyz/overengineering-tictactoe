package org.example;

import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Scanner;

/**
 * Represents a human player in the game.
 * The human player interacts with the game by providing their next move through the console.
 */
public final class HumanPlayer implements Player, Serializable {

    private static final long serialVersionUID = 1L;

    private final String playerMarker;

    private transient Scanner io = new Scanner(System.in);

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
        do {
            System.out.print("Player '" + playerMarker + "' choose an available location between [0-" + (board.getDimension()*board.getDimension()-1) + "]: ");
            location = io.nextInt();
        } while (!board.isValidMove(location));
        return location;
    }

    private void readObject(ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        io = new Scanner(System.in);
    }
}
