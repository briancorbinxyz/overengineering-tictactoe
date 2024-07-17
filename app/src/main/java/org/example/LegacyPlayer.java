package org.example;

import java.io.Serializable;
import java.util.Scanner;

@Deprecated(since = "JDK17 for a deserialization rejection use case")
public final class LegacyPlayer implements Player, Serializable {

    private static final long serialVersionUID = 0L;

    private final String playerMarker;

    public LegacyPlayer(String playerMarker) {
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
            System.out.print("Legacy Player '" + playerMarker + "' choose an available location between [0-" + (board.getDimension()*board.getDimension()-1) + "]: ");
            location = io.nextInt();
        } while (!board.isValidMove(location));
        return location;
    }

}
