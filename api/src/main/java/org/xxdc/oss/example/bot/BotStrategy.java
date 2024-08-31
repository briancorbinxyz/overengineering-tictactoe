package org.xxdc.oss.example.bot;

import java.util.concurrent.TimeUnit;
import java.util.function.ToIntFunction;
import org.xxdc.oss.example.GameState;

/**
 * An interface representing a bot strategy for a game returning a move for the current game state.
 */
public sealed interface BotStrategy
    permits AlphaBeta, Minimax, MaxN, Random, Paranoid, MonteCarloTreeSearch {

  /**
   * Returns the best move for the current game state.
   *
   * @return the index of the best move to make
   */
  int bestMove();

  /**
   * Returns a function that returns the best move for the current game state using a Random
   * strategy.
   */
  public static final ToIntFunction<GameState> RANDOM = random(BotStrategyConfig.empty());

  /**
   * Returns a function that returns the best move for the current game state using a Minimax
   * strategy.
   */
  public static final ToIntFunction<GameState> MINIMAX = minimax(BotStrategyConfig.empty());

  /**
   * Returns a function that returns the best move for the current game state using a Minimax w.
   * Alpha-Beta Pruning strategy.
   */
  public static final ToIntFunction<GameState> ALPHABETA = alphabeta(BotStrategyConfig.empty());

  /**
   * Returns a function that returns the best move for the current game state using a MaxN strategy.
   */
  public static final ToIntFunction<GameState> MAXN = maxn(BotStrategyConfig.empty());

  /**
   * Returns a function that returns the best move for the current game state using a Paranoid
   * strategy.
   */
  public static final ToIntFunction<GameState> PARANOID = paranoid(BotStrategyConfig.empty());

  /**
   * Returns a function that returns the best move for the current game state using a Monte Carlo
   * Search Tree strategy.
   */
  public static final ToIntFunction<GameState> MCTS =
      mcts(BotStrategyConfig.newBuilder().maxTimeMillis(TimeUnit.SECONDS, 2).build());

  /**
   * Returns a function that returns the best move for the current game state using a default
   * strategy (Random).
   */
  public static final ToIntFunction<GameState> DEFAULT = RANDOM;

  /**
   * Returns a function that returns the best move for the current game state using a Random
   * strategy.
   *
   * @param config the configuration for the bot strategy
   * @return a function that returns the best move for the current game state using a Random
   *     strategy
   */
  public static ToIntFunction<GameState> random(BotStrategyConfig config) {
    return (state) -> {
      var strategy = new Random(state);
      return strategy.bestMove();
    };
  }

  /**
   * Returns a function that returns the best move for the current game state using a Minimax
   * strategy.
   *
   * @param config the configuration for the bot strategy
   * @return a function that returns the best move for the current game state using a Minimax
   *     strategy
   */
  public static ToIntFunction<GameState> minimax(BotStrategyConfig config) {
    return (state) -> {
      var strategy = new Minimax(state, config);
      return strategy.bestMove();
    };
  }

  /**
   * Returns a function that returns the best move for the current game state using a Minimax w.
   * Alpha-Beta Pruning strategy.
   *
   * @param config the configuration for the bot strategy
   * @return a function that returns the best move for the current game state using a Minimax w.
   *     Alpha-Beta Pruning strategy
   */
  public static ToIntFunction<GameState> alphabeta(BotStrategyConfig config) {
    return (state) -> {
      var alphabeta = new AlphaBeta(state, config);
      return alphabeta.bestMove();
    };
  }

  /**
   * Returns a function that returns the best move for the current game state using a MaxN strategy.
   *
   * @param config the configuration for the bot strategy
   * @return a function that returns the best move for the current game state using a MaxN strategy
   */
  public static ToIntFunction<GameState> maxn(BotStrategyConfig config) {
    return (state) -> {
      var maxn = new MaxN(state, config);
      return maxn.bestMove();
    };
  }

  /**
   * Returns a function that returns the best move for the current game state using a Paranoid
   * strategy.
   *
   * @param config the configuration for the bot strategy
   * @return a function that returns the best move for the current game state using a Paranoid
   *     strategy
   */
  public static ToIntFunction<GameState> paranoid(BotStrategyConfig config) {
    return (state) -> {
      var paranoid = new Paranoid(state, config);
      return paranoid.bestMove();
    };
  }

  /**
   * Returns a function that returns the best move for the current game state using a Monte Carlo
   * Tree Search strategy.
   *
   * @param config the configuration for the bot strategy
   * @return a function that returns the best move for the current game state using a Monte Carlo
   *     Tree Search strategy
   */
  public static ToIntFunction<GameState> mcts(BotStrategyConfig config) {
    return (state) -> {
      var montecarlo = new MonteCarloTreeSearch(state, config);
      return montecarlo.bestMove();
    };
  }
}
