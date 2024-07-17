package org.example;

import java.io.Serializable;
import java.util.Scanner;

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
