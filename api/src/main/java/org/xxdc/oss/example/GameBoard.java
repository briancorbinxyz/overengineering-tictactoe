package org.xxdc.oss.example;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.reflect.InvocationTargetException;
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
   * Checks if the given player is present at the given location on the game board.
   *
   * @param playerMarker the marker representing the player to check
   * @param location the location on the game board to check
   * @return true if the player is present at the given location, false otherwise
   */
  boolean hasPlayer(String playerMarker, int location);

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

  /**
   * Returns the chain length required to win on this board, i.e. the number of consecutive markers
   * a player must place in a row, column, or diagonal to win.
   *
   * @return the chain length required to win (defaults to board dimension)
   */
  default int chainLength() {
    return dimension();
  }

  /**
   * Checks if any player can still complete a winning chain of length {@link #chainLength()} on
   * this board. Used for early draw detection — if no winnable chain remains, the game is a draw.
   *
   * @return {@code true} if at least one player can still complete a winning chain, {@code false}
   *     otherwise
   */
  default boolean hasWinnableChain() {
    return true;
  }

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
   * Creates a new {@link GameBoard} instance with the given dimension. The chain length defaults to
   * the dimension (standard Tic-Tac-Toe rules where N-in-a-row is required on an N×N board).
   *
   * @param dimension the dimension of the game board, which is the number of rows or columns
   * @return a new {@link GameBoard} instance with the specified dimension
   */
  static GameBoard withDimension(int dimension) {
    return withDimension(dimension, dimension);
  }

  /**
   * Creates a new {@link GameBoard} instance with the given dimension and chain length. The chain
   * length determines how many consecutive markers a player must place to win.
   *
   * @param dimension the dimension of the game board (N for an N×N board)
   * @param chainLength the number of consecutive markers required to win (K-in-a-row)
   * @return a new {@link GameBoard} instance with the specified dimension and chain length
   * @throws IllegalArgumentException if chainLength is less than 2 or greater than dimension
   */
  static GameBoard withDimension(int dimension, int chainLength) {
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
    // Prefer the native implementation of the game board for performance.
    GameBoard gameBoard;
    try {
      if (useNative.get()) {
        // Use the native implementation of the game board if it is available
        // using reflection.
        Class<?> gameBoardNativeImplClass =
            Class.forName("org.xxdc.oss.example.GameBoardNativeImpl");
        gameBoard =
            (GameBoard)
                gameBoardNativeImplClass
                    .getDeclaredConstructor(int.class, int.class)
                    .newInstance(dimension, chainLength);
      } else {
        gameBoard = new GameBoardLocalImpl(dimension, chainLength);
      }
    } catch (ExceptionInInitializerError
        | InstantiationException
        | IllegalAccessException
        | IllegalArgumentException
        | InvocationTargetException
        | NoSuchMethodException
        | SecurityException
        | ClassNotFoundException e) {
      // Fallback to the Java implementation.
      log.log(
          Level.WARNING,
          "Unable to use native game board, falling back to local game board: {0}({1})",
          e.getClass(),
          e.getMessage());
      useNative.set(false);
      gameBoard = new GameBoardLocalImpl(dimension, chainLength);
    }
    return gameBoard;
  }

  String[] content();
}
