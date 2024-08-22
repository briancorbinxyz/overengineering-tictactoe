package org.example;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.ToIntFunction;
import org.example.algo.MaxN;
import org.example.algo.Minimax;
import org.example.algo.MonteCarloTreeSearch;
import org.example.algo.Paranoid;

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
          var minimax = new Minimax(state);
          return minimax.bestMove();
        }),
    MAXN(
        (state) -> {
          var maxn = new MaxN(state);
          return maxn.bestMove();
        }),
    PARANOID(
        (state) -> {
          var paranoid = new Paranoid(state);
          return paranoid.bestMove();
        }),
    MCTS(
        (state) -> {
          var montecarlo = new MonteCarloTreeSearch(state, TimeUnit.SECONDS.toMillis(5));
          return montecarlo.bestMove();
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
