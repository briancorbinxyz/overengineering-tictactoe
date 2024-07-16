package org.example;

import java.util.Random;

public class BotPlayer implements Player {

    private final String playerMarker;

    private final Random random;

    public BotPlayer(String playerMarker) {
        this.playerMarker = playerMarker;
        this.random = new Random();
    }

    public String getPlayerMarker() {
        return playerMarker;
    };

    public int nextMove(GameBoard board) {
        int dimension = board.getDimension();
        int location;
        do { 
            location = random.nextInt(dimension*dimension);
        } while (!board.isValidMove(location));
        return location;
    };

    
}
