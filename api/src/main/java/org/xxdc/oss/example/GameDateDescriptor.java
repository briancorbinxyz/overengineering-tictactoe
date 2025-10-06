package org.xxdc.oss.example;

import java.util.List;
import java.util.StringJoiner;

/**
 * Produces a comprehensive description of a {@link GameState}.
 *
 * <p>The output is textual and includes: - players and current player - board dimension - a grid
 * view of the board - flat content representation - available moves (index and row/col) - last move
 * (if any) - chain status per player and terminal status - board JSON payload for
 * machine-readability
 */
public final class GameDateDescriptor {

  /**
   * Build a full description of the provided {@link GameState}.
   *
   * @param state the game state to describe
   * @return a multi-line description
   */
  public String describe(GameState state) {
    if (state == null) {
      return "<null GameState>";
    }

    GameBoard board = state.board();
    int d = board.dimension();

    StringBuilder out = new StringBuilder(512);

    // Header
    out.append("Game State Summary\n");
    out.append("- Players: ");
    out.append(formatPlayers(state.playerMarkers()));
    out.append('\n');
    out.append("- Current player index: ")
        .append(state.currentPlayerIndex())
        .append(" (marker='")
        .append(state.playerMarkers().isEmpty() ? "" : state.currentPlayer())
        .append("')\n");

    // Board basics
    out.append("- Board dimension: ").append(d).append('x').append(d).append('\n');

    // Last move
    if (state.lastMove() >= 0) {
      int r = state.lastMove() / d;
      int c = state.lastMove() % d;
      out.append("- Last move: index=")
          .append(state.lastMove())
          .append(" (row=")
          .append(r)
          .append(", col=")
          .append(c)
          .append(") by '")
          .append(state.lastPlayer())
          .append("'\n");
    } else {
      out.append("- Last move: none\n");
    }

    // Available moves
    List<Integer> moves = state.availableMoves();
    out.append("- Available moves (count=").append(moves.size()).append("): ");
    out.append(formatMovesWithCoords(moves, d)).append('\n');

    // Chain / terminal info
    out.append("- Terminal: ").append(state.isTerminal()).append('\n');
    for (String p : state.playerMarkers()) {
      out.append("  - Has chain '").append(p).append("': ").append(board.hasChain(p)).append('\n');
    }

    // Grid view
    out.append("\nBoard Grid (row-major):\n");
    out.append(renderGrid(board)).append('\n');

    // Flat content (useful for programmatic mapping)
    out.append("Flat content: ");
    out.append(formatFlatContent(board.content(), d)).append('\n');

    // JSON payload (board only)
    out.append("\nBoard JSON: ").append(board.asJsonString()).append('\n');

    return out.toString();
  }

  private static String formatPlayers(List<String> players) {
    if (players == null || players.isEmpty()) return "[]";
    StringJoiner sj = new StringJoiner(", ", "[", "]");
    for (int i = 0; i < players.size(); i++) {
      sj.add(i + ": '" + players.get(i) + "'");
    }
    return sj.toString();
  }

  private static String renderGrid(GameBoard board) {
    int d = board.dimension();
    String[] cells = board.content();
    StringBuilder sb = new StringBuilder(d * d * 4);

    // Column header
    sb.append("    ");
    for (int c = 0; c < d; c++) {
      sb.append(String.format("%2d ", c));
    }
    sb.append('\n');

    for (int r = 0; r < d; r++) {
      sb.append(String.format("%2d | ", r));
      for (int c = 0; c < d; c++) {
        String v = cells[r * d + c];
        if (v == null || v.isEmpty()) v = "."; // show empties as dots
        sb.append(padCell(v));
      }
      sb.append('\n');
    }
    return sb.toString();
  }

  private static String padCell(String v) {
    // keep grid compact for typical single-char markers, but tolerate longer
    if (v.length() == 1) return " " + v + " ";
    if (v.length() == 2) return v + " ";
    return v + " ";
  }

  private static String formatMovesWithCoords(List<Integer> moves, int d) {
    if (moves == null || moves.isEmpty()) return "[]";
    StringJoiner sj = new StringJoiner(", ", "[", "]");
    for (Integer m : moves) {
      if (m == null || m < 0) continue;
      int r = m / d;
      int c = m % d;
      sj.add(m + "(r=" + r + ",c=" + c + ")");
    }
    return sj.toString();
  }

  private static String formatFlatContent(String[] content, int d) {
    if (content == null || content.length == 0) return "[]";
    StringJoiner sj = new StringJoiner(", ", "[", "]");
    for (int i = 0; i < content.length; i++) {
      String v = content[i];
      if (v == null || v.isEmpty()) v = ".";
      sj.add(i + ":'" + v + "'");
    }
    return sj.toString();
  }
}
