package org.example;

import java.util.Scanner;

public final class HumanPlayer implements Player {

    private final String playerMarker;

    private final Scanner io;

    public HumanPlayer(String playerMarker) {
        this.playerMarker = playerMarker;
        this.io = new Scanner(System.in);
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

}
