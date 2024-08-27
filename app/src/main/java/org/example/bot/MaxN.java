package org.example.bot;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Arrays;
import org.example.GameBoard;
import org.example.GameState;

public final class MaxN implements BotStrategy {

  private static final Logger log = System.getLogger(MaxN.class.getName());

  private static final int MAX_SCORE = 100;
  private static final int MIN_SCORE = -100;

  private final GameState state;
  private final BotStrategyConfig config;

  public MaxN(GameState state) {
    this(state, BotStrategyConfig.empty());
  }

  public MaxN(GameState state, BotStrategyConfig config) {
    this.state = state;
    this.config = config;
  }

  public int bestMove() {
    int bestMove = -1;
    int[] maxScores = new int[numberOfPlayers()];
    Arrays.fill(maxScores, Integer.MIN_VALUE);

    System.out.println("Available moves: " + state.board().availableMoves());
    for (int move : state.board().availableMoves()) {
      GameBoard newBoard = state.board().withMove(currentPlayer(), move);
      int[] scores = maxn(newBoard, currentPlayerIndex(), 0);
      log(move, scores, 0);

    System.out.println("Scores: " + Arrays.toString(scores));
      if (scores[currentPlayerIndex()] > maxScores[currentPlayerIndex()]) {
        maxScores = scores;
        bestMove = move;
      }
    }
    return bestMove;
  }

  private int[] maxn(GameBoard board, int currentPlayerIdx, int depth) {
    if (board.hasChain(playerMarkerAt(currentPlayerIdx))) {
      int[] scores = new int[numberOfPlayers()];
      Arrays.fill(scores, MIN_SCORE + depth);
      scores[currentPlayerIdx] = MAX_SCORE - depth;
      return scores;
    } else if (!board.hasMovesAvailable()) {
      return new int[numberOfPlayers()]; // Draw, all scores 0
    }

    int[] bestScores = new int[numberOfPlayers()];
    Arrays.fill(bestScores, Integer.MIN_VALUE);

    for (int move : board.availableMoves()) {
      var newBoard = board.withMove(playerMarkerAt(nextPlayerIndex(currentPlayerIdx)), move);
      int[] scores = maxn(newBoard, nextPlayerIndex(currentPlayerIdx), depth + 1);

      System.out.println("Next Player: " + currentPlayerIdx + " Best Scores: " + Arrays.toString(bestScores));
      if (scores[currentPlayerIdx] > bestScores[currentPlayerIdx]) {
        bestScores = scores;
      }
    }


    return bestScores;
  }

  private String currentPlayer() {
    return state.currentPlayer();
  }

  private int currentPlayerIndex() {
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
