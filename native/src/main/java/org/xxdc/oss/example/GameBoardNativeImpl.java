package org.xxdc.oss.example;

import java.lang.System.Logger;
import java.lang.invoke.MethodHandles;
import org.xxdc.oss.example.interop.TicTacToeLibrary;

/**
 * Implements the GameBoard interface using a native library. This class manages the lifetime of the
 * native library resources and provides methods to interact with the native library.
 */
public class GameBoardNativeImpl implements GameBoard {

  // Bound to GameBoardNativeImpl lifetime, Not explicitly closeable, Accessible
  // from any thread
  // https://docs.oracle.com/en%2Fjava%2Fjavase%2F22%2Fdocs%2Fapi%2F%2F/java.base/java/lang/foreign/Arena.html
  static final Logger log = System.getLogger(MethodHandles.lookup().lookupClass().getName());

  private final TicTacToeLibrary library;

  private final GameBoard board;

  /** Constructs a new {@code GameBoardNativeImpl} instance with a default dimension of 3. */
  public GameBoardNativeImpl() {
    this(3);
  }

  /**
   * Constructs a new {@code GameBoardNativeImpl} instance with the specified dimension.
   *
   * @param dimension the dimension of the game board, must be a positive integer
   */
  public GameBoardNativeImpl(int dimension) {
    this.library = LibraryHolder.TTT.instance();
    this.board = library.newGameBoard(dimension);
  }

  @Override
  public String toString() {
    return board.toString();
  }

  @Override
  public boolean isValidMove(int location) {
    return board.isValidMove(location);
  }

  @Override
  public boolean hasChain(String playerMarker) {
    return board.hasChain(playerMarker);
  }

  @Override
  public boolean hasMovesAvailable() {
    return board.hasMovesAvailable();
  }

  @Override
  public GameBoard withMove(String playerMarker, int location) {
    if (!isValidMove(location)) {
      throw new InvalidMoveException("Invalid move: " + playerMarker + "@" + location);
    }
    return board.withMove(playerMarker, location);
  }

  @Override
  public int dimension() {
    return board.dimension();
  }

  @Override
  public String asJsonString() {
    return board.asJsonString();
  }

  private static enum LibraryHolder {
    TTT;

    private TicTacToeLibrary instance = new TicTacToeLibrary();

    public TicTacToeLibrary instance() {
      return instance;
    }
  }

  @Override
  public String[] content() {
    return board.content();
  }

  @Override
  public boolean hasPlayer(String playerMarker, int location) {
    return board.hasPlayer(playerMarker, location);
  }
}
