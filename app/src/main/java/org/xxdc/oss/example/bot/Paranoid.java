package org.xxdc.oss.example.bot;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.xxdc.oss.example.GameState;

public final class Paranoid implements BotStrategy {

  private static final Logger log = System.getLogger(Paranoid.class.getName());

  private static final int MAX_SCORE = 100;
  private static final int MIN_SCORE = -100;

  private final GameState initialState;
  private final BotStrategyConfig config;

  public Paranoid(GameState initialState) {
    this.initialState = initialState;
    this.config = BotStrategyConfig.empty();
  }

  public Paranoid(GameState state, BotStrategyConfig config) {
    this.initialState = state;
    this.config = config;
  }

  public int bestMove() {
    int bestMove = -1;
    int maxScore = Integer.MIN_VALUE;

    for (int move : initialState.board().availableMoves()) {
      GameState newState = initialState.afterPlayerMoves(move);
      int score = paranoid(newState, 0);
      log(move, score, 0);

      System.out.println("Paranoid: " + score);
      if (score > maxScore) {
        maxScore = score;
        bestMove = move;
      }
    }
    return bestMove;
  }

  private int paranoid(GameState state, int depth) {
    // Terminal state checks
    if (state.hasChain(maximizer())) {
      return MAX_SCORE - depth;
    } else if (state.lastPlayerIndex() != maximizerIndex() && state.lastPlayerHasChain()) {
      return MIN_SCORE + depth;
    } else if (!state.hasMovesAvailable() || config.exceedsMaxDepth(depth)) {
      return MIN_SCORE + depth;
    }

    if (maximizerIndex() == state.currentPlayerIndex()) {
      // Our turn: maximize our score
      int maxScore = -Integer.MAX_VALUE;
      for (int move : state.availableMoves()) {
        GameState newState = state.afterPlayerMoves(move);
        int score = paranoid(newState, depth + 1);
        maxScore = Math.max(maxScore, score);
      }
      return maxScore;
    } else {
      // Opponent's turn: minimize our score
      int minScore = Integer.MAX_VALUE;
      for (int move : state.availableMoves()) {
        GameState newState = state.afterPlayerMoves(move);
        int score = paranoid(newState, depth + 1);
        minScore = Math.min(minScore, score);
      }
      return minScore;
    }
  }

  private String maximizer() {
    return initialState.currentPlayer();
  }

  private int maximizerIndex() {
    return initialState.currentPlayerIndex();
  }

  private void log(int location, int score, int depth) {
    String indent = "-".repeat(depth);
    log.log(Level.DEBUG, "{0}{1}: Location: {2} Score: {3}", indent, maximizer(), location, score);
  }
}
