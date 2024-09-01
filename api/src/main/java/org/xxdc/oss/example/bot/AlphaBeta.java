package org.xxdc.oss.example.bot;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import org.xxdc.oss.example.GameState;

/**
 * Implements the Alpha-Beta pruning algorithm for a two-player game. The algorithm evaluates game
 * states and selects the best move for the current player. It uses a depth-limited search to
 * explore the game tree, and prunes branches that are guaranteed to be inferior. The algorithm aims
 * to maximize the score for the current player (the "maximizer") and minimize the score for the
 * opponent.
 */
public final class AlphaBeta implements BotStrategy {

  private static final Logger log = System.getLogger(AlphaBeta.class.getName());

  private static final int MIN_SCORE = -100;
  private static final int MAX_SCORE = 100;
  private static final int DRAW_SCORE = 0;

  private final String maximizer;
  private final GameState initialState;
  private final BotStrategyConfig config;

  /**
   * Constructs an instance of the AlphaBeta bot strategy with the given initial game state and
   * default configuration.
   *
   * @param initialState the initial game state for the bot to analyze
   */
  public AlphaBeta(GameState initialState) {
    this(initialState, BotStrategyConfig.newBuilder().build());
  }

  /**
   * Constructs an instance of the AlphaBeta bot strategy with the given initial game state and
   * configuration.
   *
   * @param initialState the initial game state for the bot to analyze
   * @param config the configuration settings for the bot strategy
   * @throws IllegalArgumentException if the initial game state does not have exactly two players
   */
  public AlphaBeta(GameState initialState, BotStrategyConfig config) {
    this.initialState = initialState;
    this.maximizer = initialState.currentPlayer();
    if (initialState.playerMarkers().size() != 2) {
      throw new IllegalArgumentException("Minimax AlphaBeta requires exactly two players");
    }
    this.config = config;
  }

  @Override
  public int bestMove() {
    int bestMove = -1;
    int maxScore = -Integer.MAX_VALUE;
    for (int move : initialState.availableMoves()) {
      var newState = initialState.afterPlayerMoves(move);
      int score = alphabeta(newState, false, 0);
      log(move, score, 0);
      if (score > maxScore) {
        maxScore = score;
        bestMove = move;
      }
    }
    return bestMove;
  }

  private int alphabeta(GameState state, boolean isMaximizing, int depth) {
    return alphabeta(state, isMaximizing, -Integer.MAX_VALUE, Integer.MAX_VALUE, depth);
  }

  private int alphabeta(GameState state, boolean isMaximizing, int alpha, int beta, int depth) {
    if (state.hasChain(maximizer)) {
      return MAX_SCORE - depth;
    } else if (state.hasChain(opponent(maximizer))) {
      return MIN_SCORE + depth;
    } else if (!state.hasMovesAvailable() || config.exceedsMaxDepth(depth)) {
      return DRAW_SCORE;
    }

    if (isMaximizing) {
      int value = -Integer.MAX_VALUE;
      for (int move : state.availableMoves()) {
        var newState = state.afterPlayerMoves(move);
        int score = alphabeta(newState, false, alpha, beta, depth + 1);
        value = Math.max(value, score);
        if (value > beta) {
          break;
        }
        alpha = Math.max(alpha, value);
      }
      return value;
    } else {
      int value = Integer.MAX_VALUE;
      for (int move : state.availableMoves()) {
        var newState = state.afterPlayerMoves(move);
        int score = alphabeta(newState, true, alpha, beta, depth + 1);
        value = Math.min(value, score);
        if (value < alpha) {
          break;
        }
        beta = Math.min(beta, value);
      }
      return value;
    }
  }

  private void log(int location, int score, int depth) {
    String indent = "-".repeat(depth);
    log.log(Level.DEBUG, "{0}{1}: Location: {2} Score: {3}", indent, maximizer, location, score);
  }

  private String opponent(String playerMarker) {
    return initialState.playerMarkers().stream()
        .dropWhile(playerMarker::equals)
        .findFirst()
        .orElseThrow();
  }
}
