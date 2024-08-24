package org.example.bot;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import org.example.GameBoard;
import org.example.GameState;

public final class Paranoid implements BotStrategy {

  private static final Logger log = System.getLogger(Paranoid.class.getName());

  private static final int MAX_SCORE = 100;
  private static final int MIN_SCORE = -100;

  private final GameState state;
  private final BotStrategyConfig config;

  public Paranoid(GameState state) {
    this.state = state;
    this.config = BotStrategyConfig.empty();
  }

  public Paranoid(GameState state, BotStrategyConfig config) {
    this.state = state;
    this.config = config;
  }

  public int bestMove() {
    int bestMove = -1;
    int maxScore = Integer.MIN_VALUE;

    for (int move : state.board().availableMoves()) {
      GameBoard newBoard = state.board().withMove(maximizer(), move);
      int score = paranoid(newBoard, nextPlayerIndex(maximizerIndex()), 0);
      log(move, score, 0);

      if (score > maxScore) {
        maxScore = score;
        bestMove = move;
      }
    }
    return bestMove;
  }

  private int paranoid(GameBoard board, int currentPlayerIdx, int depth) {
    // Terminal state checks
    if (board.hasChain(maximizer())) {
      return MAX_SCORE - depth;
    } else if (board.hasChain(playerMarkerAt(currentPlayerIdx))) {
      return MIN_SCORE + depth;
    } else if (!board.hasMovesAvailable() || config.exceedsMaxDepth(depth)) {
      return MIN_SCORE + depth;
    }

    if (maximizerIndex() == currentPlayerIdx) {
      // Our turn: maximize our score
      int maxScore = -Integer.MAX_VALUE;
      for (int move : board.availableMoves()) {
        GameBoard newBoard = board.withMove(playerMarkerAt(currentPlayerIdx), move);
        int score = paranoid(newBoard, nextPlayerIndex(currentPlayerIdx), depth + 1);
        maxScore = Math.max(maxScore, score);
      }
      return maxScore;
    } else {
      // Opponent's turn: minimize our score
      int minScore = Integer.MAX_VALUE;
      for (int move : board.availableMoves()) {
        GameBoard newBoard = board.withMove(playerMarkerAt(currentPlayerIdx), move);
        int score = paranoid(newBoard, nextPlayerIndex(currentPlayerIdx), depth + 1);
        minScore = Math.min(minScore, score);
      }
      return minScore;
    }
  }

  private String maximizer() {
    return state.currentPlayer();
  }

  private int maximizerIndex() {
    return state.currentPlayerIndex();
  }

  private int numberOfPlayers() {
    return state.playerMarkers().size();
  }

  private int nextPlayerIndex(int playerIndex) {
    return (playerIndex + 1) % numberOfPlayers();
  }

  private String playerMarkerAt(int playerIndex) {
    return state.playerMarkers().get(playerIndex);
  }

  private void log(int location, int score, int depth) {
    String indent = "-".repeat(depth);
    log.log(Level.DEBUG, "{0}{1}: Location: {2} Score: {3}", indent, maximizer(), location, score);
  }
}
