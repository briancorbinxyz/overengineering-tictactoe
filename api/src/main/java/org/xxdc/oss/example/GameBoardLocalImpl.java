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
 * @param content the current state of the board, represented as a 1D array of player marker strings
 */
public record GameBoardLocalImpl(int dimension, String[] content)
    implements Serializable, GameBoard {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new {@code GameBoardLocalImpl} instance with the specified dimension and
   * initializes the content array with null values.
   *
   * @param dimension the dimension of the game board
   */
  public GameBoardLocalImpl(int dimension) {
    this(dimension, new String[dimension * dimension]);
  }

  public GameBoardLocalImpl {
    if (content.length != dimension * dimension) {
      throw new IllegalArgumentException("Content must be of length " + dimension * dimension);
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
    // check rows
    for (int i = 0; i < dimension; i++) {
      int chain = 0;
      for (int j = 0; j < dimension; j++) {
        if (playerMarker.equals(content[i * dimension + j])) {
          chain++;
        }
        if (chain == dimension) {
          return true;
        }
      }
    }
    // check columns
    for (int i = 0; i < dimension; i++) {
      int chain = 0;
      for (int j = 0; j < dimension; j++) {
        if (playerMarker.equals(content[j * dimension + i])) {
          chain++;
        }
        if (chain == dimension) {
          return true;
        }
      }
    }
    // check diagonals
    {
      int chain = 0;
      for (int i = 0; i < dimension; i++) {
        if (playerMarker.equals(content[i + (dimension * (i + 1)) - dimension])) {
          chain++;
        }
        if (chain == dimension) {
          return true;
        }
      }
    }
    {
      int chain = 0;
      for (int i = 0; i < dimension; i++) {
        if (playerMarker.equals(content[(dimension * (i + 1)) - (i + 1)])) {
          chain++;
        }
        if (chain == dimension) {
          return true;
        }
      }
    }
    return false;
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
    return new GameBoardLocalImpl(dimension, boardCopy);
  }

  @Override
  public String asJsonString() {
    StringBuilder json = new StringBuilder();
    json.append("{");
    json.append("\"dimension\":").append(dimension()).append(",");
    json.append("\"content\":")
        .append(
            Arrays.stream(content())
                .map(m -> m == null ? "null" : "\"" + m + "\"")
                .collect(Collectors.joining(",", "[", "]")));
    json.append("}");
    return json.toString();
  }

  public GameBoard clone() {
    return new GameBoardLocalImpl(dimension, getBoardCopy());
  }

  private String[] getBoardCopy() {
    String[] boardCopy = new String[dimension * dimension];
    System.arraycopy(content, 0, boardCopy, 0, boardCopy.length);
    return boardCopy;
  }
}
