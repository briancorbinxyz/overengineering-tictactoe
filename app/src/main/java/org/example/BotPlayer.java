package org.example;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.random.RandomGenerator;

/**
 * Represents a bot player in the game. The bot player uses a random number generator to make moves on the game board.
 */
public final class BotPlayer implements Player, Serializable {

    private final String playerMarker;

    private final RandomGenerator random;

    public BotPlayer(String playerMarker, RandomGenerator randomGenerator) {
        this.playerMarker = playerMarker;
        this.random = randomGenerator;
    }

    public BotPlayer(String playerMarker) {
        // OVER-ENGINEER: Cryptographically secure by default
        this(playerMarker, new SecureRandom());
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
