package org.example.algo;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.List;
import org.example.GameBoard;
import org.example.GameState;

public class AlphaBeta {

  private static final Logger log = System.getLogger(Minimax.class.getName());

  private static final int MIN_SCORE = -100;
  private static final int MAX_SCORE = 100;
  private static final int DRAW_SCORE = 0;

  private final String maximizer;
  private final GameBoard board;
  private final List<String> playerMarkers;

  public AlphaBeta(GameState state) {
    this.board = state.board();
    this.maximizer = state.currentPlayer();
    if (state.playerMarkers().size() != 2) {
      throw new IllegalArgumentException("Minimax AlphaBeta requires exactly two players");
    }
    this.playerMarkers = state.playerMarkers();
  }

  public int bestMove() {
    int bestMove = -1;
    int maxScore = -Integer.MAX_VALUE;
    for (int move : board.availableMoves()) {
      var newBoard = board.withMove(maximizer, move);
      int score = alphabeta(newBoard, false, 0);
      log(move, score, 0);
      if (score > maxScore) {
        maxScore = score;
        bestMove = move;
      }
    }
    return bestMove;
  }

  private int alphabeta(GameBoard board, boolean isMaximizing, int depth) {
    return alphabeta(board, isMaximizing, -Integer.MAX_VALUE, Integer.MAX_VALUE, depth);
  }

  private int alphabeta(GameBoard board, boolean isMaximizing, int alpha, int beta, int depth) {
    if (board.hasChain(maximizer)) {
      return MAX_SCORE - depth;
    } else if (board.hasChain(opponent(maximizer))) {
      return MIN_SCORE + depth;
    } else if (!board.hasMovesAvailable()) {
      return DRAW_SCORE;
    }

    if (isMaximizing) {
      int value = -Integer.MAX_VALUE;
      for (int move : board.availableMoves()) {
        var newBoard = board.withMove(maximizer, move);
        int score = alphabeta(newBoard, false, depth + 1);
        value = Math.max(value, score);
        if (value > beta) {
          break;
        }
        alpha = Math.max(alpha, value);
      }
      return value;
    } else {
      int value = Integer.MAX_VALUE;
      for (int move : board.availableMoves()) {
        var newBoard = board.withMove(opponent(maximizer), move);
        int score = alphabeta(newBoard, true, depth + 1);
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
    return playerMarkers.stream().dropWhile(playerMarker::equals).findFirst().orElseThrow();
  }
}
