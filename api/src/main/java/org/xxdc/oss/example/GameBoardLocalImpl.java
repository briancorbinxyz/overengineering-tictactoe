package org.xxdc.oss.example;

import java.io.Serializable;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Represents a game board for a game, such as tic-tac-toe. The board has a specified dimension, and
 * stores the current state of the game in a 1D array. Provides methods to check the validity of
 * moves, place player markers, check for a winner, and get a string representation of the board.
 *
 * @param dimension the dimension of the board
 * @param chainLength the number of consecutive markers required to win
 * @param content the current state of the board, represented as a 1D array of player marker strings
 */
public record GameBoardLocalImpl(int dimension, int chainLength, String[] content)
    implements Serializable, GameBoard {

  private static final long serialVersionUID = 2L;

  /**
   * Constructs a new {@code GameBoardLocalImpl} instance with the specified dimension. Chain length
   * defaults to dimension (standard N-in-a-row on N×N board).
   *
   * @param dimension the dimension of the game board
   */
  public GameBoardLocalImpl(int dimension) {
    this(dimension, dimension, new String[dimension * dimension]);
  }

  /**
   * Constructs a new {@code GameBoardLocalImpl} instance with the specified dimension and chain
   * length.
   *
   * @param dimension the dimension of the game board
   * @param chainLength the number of consecutive markers required to win
   */
  public GameBoardLocalImpl(int dimension, int chainLength) {
    this(dimension, chainLength, new String[dimension * dimension]);
  }

  public GameBoardLocalImpl {
    if (content.length != dimension * dimension) {
      throw new IllegalArgumentException("Content must be of length " + dimension * dimension);
    }
    if (chainLength < 2) {
      throw new IllegalArgumentException("Chain length must be at least 2, got: " + chainLength);
    }
    if (chainLength > dimension) {
      throw new IllegalArgumentException(
          "Chain length must not exceed dimension, got chainLength="
              + chainLength
              + " for dimension="
              + dimension);
    }
  }

  @Override
  public String toString() {
    return boardAsString();
  }

  private String boardAsString() {
    String boardString = "";
    for (int i = 0; i < dimension; i++) {
      for (int j = 0; j < dimension; j++) {
        int index = j + i * dimension;
        boardString += contentElseBlank(content[index]);
      }
      boardString += "\n";
    }
    return boardString;
  }

  @Override
  public boolean isValidMove(int location) {
    return location >= 0 && location < content.length && content[location] == null;
  }

  @Override
  public boolean hasChain(String playerMarker) {
    return hasChainInRows(playerMarker)
        || hasChainInColumns(playerMarker)
        || hasChainInDiagonals(playerMarker);
  }

  private boolean hasChainInRows(String playerMarker) {
    for (int row = 0; row < dimension; row++) {
      if (hasChainInLine(playerMarker, row * dimension, 1, dimension)) {
        return true;
      }
    }
    return false;
  }

  private boolean hasChainInColumns(String playerMarker) {
    for (int col = 0; col < dimension; col++) {
      if (hasChainInLine(playerMarker, col, dimension, dimension)) {
        return true;
      }
    }
    return false;
  }

  private boolean hasChainInDiagonals(String playerMarker) {
    // Downward-right diagonals (\)
    for (int startRow = 0; startRow <= dimension - chainLength; startRow++) {
      for (int startCol = 0; startCol <= dimension - chainLength; startCol++) {
        int length = dimension - Math.max(startRow, startCol);
        if (hasChainInLine(playerMarker, startRow * dimension + startCol, dimension + 1, length)) {
          return true;
        }
      }
    }
    // Downward-left diagonals (/)
    for (int startRow = 0; startRow <= dimension - chainLength; startRow++) {
      for (int startCol = chainLength - 1; startCol < dimension; startCol++) {
        int length = Math.min(dimension - startRow, startCol + 1);
        if (hasChainInLine(playerMarker, startRow * dimension + startCol, dimension - 1, length)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Checks for chainLength consecutive markers along a line defined by a start index, step size,
   * and number of cells.
   */
  private boolean hasChainInLine(String playerMarker, int start, int step, int count) {
    int chain = 0;
    for (int i = 0; i < count; i++) {
      int index = start + i * step;
      if (index >= 0 && index < content.length && playerMarker.equals(content[index])) {
        chain++;
        if (chain == chainLength) {
          return true;
        }
      } else {
        chain = 0;
      }
    }
    return false;
  }

  @Override
  public boolean hasWinnableChain() {
    return hasWinnableInRows() || hasWinnableInColumns() || hasWinnableInDiagonals();
  }

  private boolean hasWinnableInRows() {
    for (int row = 0; row < dimension; row++) {
      if (hasWinnableWindow(row * dimension, 1, dimension)) return true;
    }
    return false;
  }

  private boolean hasWinnableInColumns() {
    for (int col = 0; col < dimension; col++) {
      if (hasWinnableWindow(col, dimension, dimension)) return true;
    }
    return false;
  }

  private boolean hasWinnableInDiagonals() {
    // Downward-right diagonals (\)
    for (int startRow = 0; startRow <= dimension - chainLength; startRow++) {
      for (int startCol = 0; startCol <= dimension - chainLength; startCol++) {
        int length = dimension - Math.max(startRow, startCol);
        if (hasWinnableWindow(startRow * dimension + startCol, dimension + 1, length)) return true;
      }
    }
    // Downward-left diagonals (/)
    for (int startRow = 0; startRow <= dimension - chainLength; startRow++) {
      for (int startCol = chainLength - 1; startCol < dimension; startCol++) {
        int length = Math.min(dimension - startRow, startCol + 1);
        if (hasWinnableWindow(startRow * dimension + startCol, dimension - 1, length)) return true;
      }
    }
    return false;
  }

  /**
   * Checks if any sliding window of size chainLength along a line contains markers from at most one
   * player — meaning that window is still winnable by someone.
   */
  private boolean hasWinnableWindow(int start, int step, int count) {
    for (int windowStart = 0; windowStart <= count - chainLength; windowStart++) {
      if (isWindowWinnable(start + windowStart * step, step)) return true;
    }
    return false;
  }

  /** Checks if a single window of chainLength cells contains markers from at most one player. */
  private boolean isWindowWinnable(int start, int step) {
    String owner = null;
    for (int i = 0; i < chainLength; i++) {
      int index = start + i * step;
      if (index < 0 || index >= content.length) return false;
      String cell = content[index];
      if (cell == null) continue;
      if (owner == null) {
        owner = cell;
      } else if (!owner.equals(cell)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean hasMovesAvailable() {
    return Arrays.stream(content).anyMatch(m -> m == null);
  }

  private String contentElseBlank(String unit) {
    return unit == null ? "\u005F" : unit;
  }

  @Override
  public GameBoardLocalImpl withMove(String playerMarker, int location) {
    if (!isValidMove(location)) {
      throw new InvalidMoveException("Invalid move: " + playerMarker + "@" + location);
    }
    String[] boardCopy = getBoardCopy();
    boardCopy[location] = playerMarker;
    return new GameBoardLocalImpl(dimension, chainLength, boardCopy);
  }

  @Override
  public String asJsonString() {
    StringBuilder json = new StringBuilder();
    json.append("{");
    json.append("\"dimension\":").append(dimension()).append(",");
    json.append("\"chainLength\":").append(chainLength()).append(",");
    json.append("\"content\":")
        .append(
            Arrays.stream(content())
                .map(m -> m == null ? "null" : "\"" + m + "\"")
                .collect(Collectors.joining(",", "[", "]")));
    json.append("}");
    return json.toString();
  }

  public GameBoard clone() {
    return new GameBoardLocalImpl(dimension, chainLength, getBoardCopy());
  }

  @Override
  public boolean hasPlayer(String playerMarker, int location) {
    return location >= 0
        && location < content.length
        && content[location] != null
        && content[location].equals(playerMarker);
  }

  private String[] getBoardCopy() {
    String[] boardCopy = new String[dimension * dimension];
    System.arraycopy(content, 0, boardCopy, 0, boardCopy.length);
    return boardCopy;
  }
}
