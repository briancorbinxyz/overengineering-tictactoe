package org.xxdc.oss.example.bot;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Arrays;
import org.xxdc.oss.example.GameState;

/**
 * Implements the MaxN bot strategy for a game. The MaxN strategy tries to maximize the score for
 * the current player, while considering the scores of all other players. It uses a recursive
 * algorithm to explore the game tree and find the best move.
 */
public final class MaxN implements BotStrategy {

  private static final Logger log = System.getLogger(MaxN.class.getName());

  private static final int MAX_SCORE = 100;
  private static final int MIN_SCORE = -100;

  private final GameState initialState;
  private final BotStrategyConfig config;

  /**
   * Constructs a new MaxN bot strategy with the given initial game state and default configuration.
   *
   * @param initialState the initial game state to use for the bot strategy
   */
  public MaxN(GameState initialState) {
    this(initialState, BotStrategyConfig.empty());
  }

  /**
   * Constructs a new MaxN bot strategy with the given initial game state and configuration.
   *
   * @param initialState the initial game state to use for the bot strategy
   * @param config the configuration settings for the bot strategy
   */
  public MaxN(GameState initialState, BotStrategyConfig config) {
    this.initialState = initialState;
    this.config = config;
  }

  @Override
  public int bestMove() {
    int bestMove = -1;
    int[] maxScores = new int[numberOfPlayers()];
    Arrays.fill(maxScores, Integer.MIN_VALUE);

    for (int move : initialState.availableMoves()) {
      var newState = initialState.afterPlayerMoves(move);
      int[] scores = maxn(newState, 0);
      log(move, scores, 0);

      if (scores[newState.lastPlayerIndex()] > maxScores[newState.lastPlayerIndex()]) {
        maxScores = scores;
        bestMove = move;
      }
    }
    return bestMove;
  }

  private int[] maxn(GameState state, int depth) {
    if (state.lastPlayerHasChain()) {
      int[] scores = new int[numberOfPlayers()];
      Arrays.fill(scores, MIN_SCORE + depth);
      scores[state.lastPlayerIndex()] = MAX_SCORE - depth;
      return scores;
    } else if (!state.hasMovesAvailable() || config.exceedsMaxDepth(depth)) {
      return new int[numberOfPlayers()]; // Draw, all scores 0
    }

    int[] bestScores = new int[numberOfPlayers()];
    Arrays.fill(bestScores, Integer.MIN_VALUE);

    for (int move : state.availableMoves()) {
      var newState = state.afterPlayerMoves(move);
      int[] scores = maxn(newState, depth + 1);

      if (scores[state.currentPlayerIndex()] > bestScores[state.currentPlayerIndex()]) {
        bestScores = scores;
      }
    }

    return bestScores;
  }

  private String currentPlayer() {
    return initialState.currentPlayer();
  }

  private int numberOfPlayers() {
    return initialState.playerMarkers().size();
  }

  private void log(int location, int[] scores, int depth) {
    String indent = "-".repeat(depth);
    if (log.isLoggable(Level.DEBUG)) {
      log.log(
          Level.DEBUG,
          "{0}{1}: Location: {2} Scores: {3}",
          indent,
          currentPlayer(),
          location,
          Arrays.toString(scores));
    }
  }
}
