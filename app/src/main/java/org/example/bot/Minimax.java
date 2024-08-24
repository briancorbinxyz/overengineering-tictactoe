package org.example.bot;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.List;
import org.example.GameBoard;
import org.example.GameState;

public final class Minimax implements BotStrategy {

  private static final Logger log = System.getLogger(Minimax.class.getName());

  private static final int MIN_SCORE = -100;
  private static final int MAX_SCORE = 100;
  private static final int DRAW_SCORE = 0;

  private final String maximizer;
  private final GameBoard board;
  private final List<String> playerMarkers;
  private final BotStrategyConfig config;

  public Minimax(GameState state) {
    this(state, BotStrategyConfig.empty());
  }

  public Minimax(GameState state, BotStrategyConfig config) {
    this.board = state.board();
    this.maximizer = state.currentPlayer();
    if (state.playerMarkers().size() != 2) {
      throw new IllegalArgumentException("Minimax requires exactly two players");
    }
    this.playerMarkers = state.playerMarkers();
    this.config = config;
  }

  public int bestMove() {
    int bestMove = -1;
    int maxScore = -Integer.MAX_VALUE;
    for (int move : board.availableMoves()) {
      var newBoard = board.withMove(maximizer, move);
      int score = minimax(newBoard, false, 0);
      log(move, score, 0);
      if (score > maxScore) {
        maxScore = score;
        bestMove = move;
      }
    }
    return bestMove;
  }

  private int minimax(GameBoard board, boolean isMaximizing, int depth) {
    if (board.hasChain(maximizer)) {
      return MAX_SCORE - depth;
    } else if (board.hasChain(opponent(maximizer))) {
      return MIN_SCORE + depth;
    } else if (!board.hasMovesAvailable() || config.exceedsMaxDepth(depth)) {
      return DRAW_SCORE;
    }

    if (isMaximizing) {
      int value = -Integer.MAX_VALUE;
      for (int move : board.availableMoves()) {
        var newBoard = board.withMove(maximizer, move);
        int score = minimax(newBoard, false, depth + 1);
        value = Math.max(value, score);
      }
      return value;
    } else {
      int value = Integer.MAX_VALUE;
      for (int move : board.availableMoves()) {
        var newBoard = board.withMove(opponent(maximizer), move);
        int score = minimax(newBoard, true, depth + 1);
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
    return playerMarkers.stream().dropWhile(playerMarker::equals).findFirst().orElseThrow();
  }
}
