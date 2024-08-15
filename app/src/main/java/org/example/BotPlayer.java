package org.example;

import java.io.Serializable;
import java.security.SecureRandom;
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

    public int nextMove(GameBoard board) {
        return botStrategy.apply(board);
    }

    public static enum BotStrategy {
        RANDOM((board) -> {
            var random = new SecureRandom();
            int dimension = board.dimension();
            int location;
            do {
                location = random.nextInt(dimension * dimension);
            } while (!board.isValidMove(location));
            return location;
        }),
        ;

        public int apply(GameBoard board) {
            return strategyFunction.applyAsInt(board);
        }

        private BotStrategy(ToIntFunction<GameBoard> strategyFunction) {
            this.strategyFunction = strategyFunction;
        }

        private ToIntFunction<GameBoard> strategyFunction;
    }
}
