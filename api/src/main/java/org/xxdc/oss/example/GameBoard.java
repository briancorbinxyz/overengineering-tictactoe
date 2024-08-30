package org.xxdc.oss.example;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents a game board for a game. The game board has a square dimension and contains a grid of
 * game pieces. This interface defines the operations that can be performed on the game board.
 */
public interface GameBoard extends JsonSerializable {

  static final Logger log = System.getLogger(GameBoard.class.getName());

  static final AtomicBoolean useNative = new AtomicBoolean(true);

  /**
   * Checks if the given location on the game board is a valid move (i.e. an available location).
   *
   * @param location the location on the game board to check
   * @return true if the location is a valid move, false otherwise
   */
  boolean isValidMove(int location);

  /**
   * Returns a list of available moves on the game board.
   *
   * @return a list of available moves on the game board
   */
  default List<Integer> availableMoves() {
    int d = dimension();
    List<Integer> availableMoves = new ArrayList<Integer>(d * d);
    for (int i = 0; i < d; i++) {
      for (int j = 0; j < d; j++) {
        if (isValidMove(i * d + j)) {
          availableMoves.add(i * d + j);
        }
      }
    }
    return availableMoves;
  }

  default boolean isEmpty() {
    for (int i = 0; i < dimension() * dimension(); i++) {
      if (!isValidMove(i)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks if the given player has a winning chain of connected game pieces on the game board.
   *
   * @param playerMarker the marker representing the player to check for a chain
   * @return true if the player has a chain of connected game pieces, false otherwise
   */
  boolean hasChain(String playerMarker);

  /**
   * Checks if there are any available moves on the game board.
   *
   * @return true if there are any available moves, false otherwise
   */
  boolean hasMovesAvailable();

  /**
   * Creates a new {@link GameBoard} instance with the given player's move made at the specified
   * location within the game board dimension x dimension
   *
   * @param playerMarker the marker representing the player making the move
   * @param location the location on the game board where the player is making the move
   * @return a ne {@link GameBoard} instance with the player's move applied
   */
  GameBoard withMove(String playerMarker, int location);

  /**
   * Returns the dimension of the game board, which is the number of rows or columns.
   *
   * @return the dimension of the game board
   */
  int dimension();

  ///
  /// Converts the game board to a JSON string representation for serialization. Format
  /// corresponds to the following JSON schema with content as a 1D array of strings of size
  /// dimension x dimension.
  ///
  /// ```javascript
  /// { "dimension": int, "content": [ string, string, ..., string ] } }
  /// ```
  /// @return the game board as a JSON string
  /// @see JsonSerializable
  String asJsonString();

  /**
   * Returns a string representation of the game board for presentation to the player.
   *
   * @return a string representation of the game board.
   */
  String toString();

  /**
   * Creates a new {@link GameBoard} instance with the given dimension.
   *
   * @param dimension the dimension of the game board, which is the number of rows or columns
   * @return a new {@link GameBoard} instance with the specified dimension
   */
  static GameBoard with(int dimension) {
    // Prefer the native implementation of the game board for performance.
    GameBoard gameBoard;
    try {
      if (useNative.get()) {
        gameBoard = new GameBoardNativeImpl(dimension);
      } else {
        gameBoard = new GameBoardDefaultImpl(dimension);
      }
    } catch (Exception e) {
      // Fallback to the Java implementation.
      log.log(Level.WARNING, "Unable to use native logger, falling back to default logger: {0}", e.getMessage());
      useNative.set(false);
      gameBoard = new GameBoardDefaultImpl(dimension);
    }
    return gameBoard;
  }
}
