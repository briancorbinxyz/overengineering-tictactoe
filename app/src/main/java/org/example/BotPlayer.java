package org.example;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.function.ToIntBiFunction;
import java.util.function.ToIntFunction;

/**
 * Represents a bot player in the game. The bot player uses a random number generator to make moves
 * on the game board.
 */
public record BotPlayer(BotStrategy botStrategy)
        implements Player, Serializable {

    private static final long serialVersionUID = 1L;

    public BotPlayer() {
        this(BotStrategy.RANDOM);
    }

    @Override   
    public int nextMove(String playerMarker, GameBoard board) {
        return botStrategy.apply(playerMarker, board);
    }

    public static enum BotStrategy {
        RANDOM((_, board) -> {
            var random = new SecureRandom();
            int dimension = board.dimension();
            int location;
            do {
                location = random.nextInt(dimension * dimension);
            } while (!board.isValidMove(location));
            return location;
        }),
        ;

        public int apply(String playerMarker, GameBoard board) {
            return strategyFunction.applyAsInt(playerMarker, board);
        }

        private BotStrategy(ToIntBiFunction<String, GameBoard> strategyFunction) {
            this.strategyFunction = strategyFunction;
        }

        private ToIntBiFunction<String, GameBoard> strategyFunction;
    }
}
