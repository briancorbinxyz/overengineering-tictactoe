package org.xxdc.oss.example.bot;

import java.util.concurrent.TimeUnit;
import java.util.function.ToIntFunction;

import org.xxdc.oss.example.GameState;

public sealed interface BotStrategy
    permits AlphaBeta, Minimax, MaxN, Random, Paranoid, MonteCarloTreeSearch {
  public int bestMove();

  public static final ToIntFunction<GameState> RANDOM = random(BotStrategyConfig.empty());
  public static final ToIntFunction<GameState> MINIMAX = minimax(BotStrategyConfig.empty());
  public static final ToIntFunction<GameState> ALPHABETA = alphabeta(BotStrategyConfig.empty());
  public static final ToIntFunction<GameState> MAXN = maxn(BotStrategyConfig.empty());
  public static final ToIntFunction<GameState> PARANOID = paranoid(BotStrategyConfig.empty());
  public static final ToIntFunction<GameState> MCTS =
      mcts(BotStrategyConfig.newBuilder().maxTimeMillis(TimeUnit.SECONDS, 2).build());
  public static final ToIntFunction<GameState> DEFAULT = RANDOM;

  public static ToIntFunction<GameState> random(BotStrategyConfig config) {
    return (state) -> {
      var strategy = new Random(state);
      return strategy.bestMove();
    };
  }

  public static ToIntFunction<GameState> minimax(BotStrategyConfig config) {
    return (state) -> {
      var strategy = new Minimax(state, config);
      return strategy.bestMove();
    };
  }

  public static ToIntFunction<GameState> alphabeta(BotStrategyConfig config) {
    return (state) -> {
      var alphabeta = new AlphaBeta(state, config);
      return alphabeta.bestMove();
    };
  }

  public static ToIntFunction<GameState> maxn(BotStrategyConfig config) {
    return (state) -> {
      var maxn = new MaxN(state, config);
      return maxn.bestMove();
    };
  }

  public static ToIntFunction<GameState> paranoid(BotStrategyConfig config) {
    return (state) -> {
      var paranoid = new Paranoid(state, config);
      return paranoid.bestMove();
    };
  }

  public static ToIntFunction<GameState> mcts(BotStrategyConfig config) {
    return (state) -> {
      var montecarlo = new MonteCarloTreeSearch(state, config);
      return montecarlo.bestMove();
    };
  }
}
