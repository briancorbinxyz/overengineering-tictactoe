package org.example;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.function.ToIntFunction;

import org.example.algo.MaxN;
import org.example.algo.Minimax;

/**
 * Represents a bot player in the game. The bot player uses a random number generator to make moves
 * on the game board.
 */
public record BotPlayer(BotStrategy botStrategy) implements Player, Serializable {

  private static final long serialVersionUID = 1L;

  public BotPlayer() {
    this(BotStrategy.RANDOM);
  }

  @Override
  public int nextMove(GameState state) {
    return botStrategy.apply(state);
  }

  public static enum BotStrategy {
    RANDOM(
        (state) -> {
          var random = new SecureRandom();
          var availableMoves = state.board().availableMoves();
          return availableMoves.get(random.nextInt(availableMoves.size()));
        }),
    MINIMAX(
        (state) -> {
          var minimax = new Minimax(state.currentPlayer(), state.board());
          return minimax.bestMove();
        }),
    MAXN(
        (state) -> {
          var maxn = new MaxN(state);
          return maxn.bestMove();
        }),
        ;

    public int apply(GameState state) {
      return strategyFunction.applyAsInt(state);
    }

    private BotStrategy(ToIntFunction<GameState> strategyFunction) {
      this.strategyFunction = strategyFunction;
    }

    private ToIntFunction<GameState> strategyFunction;
  }
}
