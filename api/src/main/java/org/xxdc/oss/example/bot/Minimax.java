package org.xxdc.oss.example.bot;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import org.xxdc.oss.example.GameState;

/**
 * Implements a Minimax algorithm-based bot strategy for a game. The Minimax algorithm is a
 * decision-making algorithm used in game theory and artificial intelligence to find the optimal
 * move for a player, assuming the opponent plays optimally. This implementation of the Minimax
 * algorithm is used to determine the best move for the current player in the game.
 */
public final class Minimax implements BotStrategy {

  private static final Logger log = System.getLogger(Minimax.class.getName());

  private static final int MIN_SCORE = -100;
  private static final int MAX_SCORE = 100;
  private static final int DRAW_SCORE = 0;

  private final String maximizer;
  private final BotStrategyConfig config;
  private final GameState initialState;

  /**
   * Constructs a new Minimax instance with the given initial game state and default bot strategy
   * configuration.
   *
   * @param initialState the initial game state to start the Minimax algorithm from
   */
  public Minimax(GameState initialState) {
    this(initialState, BotStrategyConfig.empty());
  }

  /**
   * Constructs a new Minimax instance with the given initial game state and bot strategy
   * configuration.
   *
   * @param initialState the initial game state to start the Minimax algorithm from
   * @param config the bot strategy configuration to use for this Minimax instance
   * @throws IllegalArgumentException if the initial game state does not have exactly two players
   */
  public Minimax(GameState initialState, BotStrategyConfig config) {
    this.initialState = initialState;
    this.maximizer = initialState.currentPlayer();
    if (initialState.playerMarkers().size() != 2) {
      throw new IllegalArgumentException("Minimax requires exactly two players");
    }
    this.config = config;
  }

  @Override
  public int bestMove() {
    int bestMove = -1;
    int maxScore = -Integer.MAX_VALUE;
    for (int move : initialState.availableMoves()) {
      var newState = initialState.afterPlayerMoves(move);
      int score = minimax(newState, false, 0);
      log(move, score, 0);
      if (score > maxScore) {
        maxScore = score;
        bestMove = move;
      }
    }
    return bestMove;
  }

  private int minimax(GameState state, boolean isMaximizing, int depth) {
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
        int score = minimax(newState, false, depth + 1);
        value = Math.max(value, score);
      }
      return value;
    } else {
      int value = Integer.MAX_VALUE;
      for (int move : state.availableMoves()) {
        var newState = state.afterPlayerMoves(move);
        int score = minimax(newState, true, depth + 1);
        value = Math.min(value, score);
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
