package org.example.interop;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.ref.Cleaner;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.example.GameBoard;

class TicTacToeGameBoard implements GameBoard {

  private static final Logger log =
      System.getLogger(MethodHandles.lookup().lookupClass().getName());

  private final MemorySegment board;

  private final Linker linker = Linker.nativeLinker();

  private final SymbolLookup libTicTacToe;

  private final PlayerIds playerIds;

  private final Map<String, Integer> playerMarkerToId;
  private final Map<Integer, String> idToPlayerMarker;

  private final Cleaner cleaner;

  private static class CleanupTask implements Runnable {
    private final MemorySegment board;
    private final MethodHandle freeGameBoard;

    public CleanupTask(MemorySegment board, MethodHandle freeGameBoard) {
      this.board = board;
      this.freeGameBoard = freeGameBoard;
    }

    public void run() {
      try {
        log.log(Level.DEBUG, "Cleaning up native resources for TicTacToeGameBoard");
        freeGameBoard.invoke(board);
      } catch (Throwable t) {
        log.log(Level.ERROR, "Failed to free game board", t);
      }
    }
  }

  private MethodHandle newGameBoard;
  private MethodHandle freeGameBoard;
  private MethodHandle getDimension;
  private MethodHandle withMove;
  private MethodHandle getValueAtIndex;
  private MethodHandle getGameBoardIsFull;
  private MethodHandle getGameBoardHasChain;

  public TicTacToeGameBoard(int dimension, SymbolLookup libTicTacToe, Cleaner cleaner) {
    this.libTicTacToe = libTicTacToe;
    this.playerMarkerToId = new HashMap<>();
    this.idToPlayerMarker = new HashMap<>();
    this.playerIds = new PlayerIds(1);
    this.initGameBoardMethods();
    this.board = newGameBoard(dimension);
    this.cleaner = cleaner;
    cleaner.register(this, new CleanupTask(board, freeGameBoard));
  }

  TicTacToeGameBoard(
      MemorySegment board,
      Map<String, Integer> playerMarkerToId,
      Map<Integer, String> idToPlayerMarker,
      int initialValue,
      SymbolLookup libTicTacToe,
      Cleaner cleaner) {
    this.libTicTacToe = libTicTacToe;
    this.playerMarkerToId = new HashMap<>(playerMarkerToId);
    this.idToPlayerMarker = new HashMap<>(idToPlayerMarker);
    this.playerIds = new PlayerIds(initialValue);
    this.initGameBoardMethods();
    this.board = board;
    this.cleaner = cleaner;
    cleaner.register(this, new CleanupTask(board, freeGameBoard));
  }

  @Override
  public boolean isValidMove(int location) {
    return location >= 0 && location < dimension() * dimension() && getValueAtIndex(location) == 0;
  }

  @Override
  public boolean hasChain(String playerMarker) {
    return playerMarkerToId.containsKey(playerMarker)
        && getGameBoardHasChain(playerMarkerToId.get(playerMarker));
  }

  @Override
  public boolean hasMovesAvailable() {
    return !getGameBoardIsFull();
  }

  @Override
  public GameBoard withMove(String playerMarker, int location) {
    if (!playerMarkerToId.containsKey(playerMarker)) {
      int id = playerIds.getNextIdAndIncrement();
      playerMarkerToId.put(playerMarker, id);
      idToPlayerMarker.put(id, playerMarker);
    }

    try {
      int playerId = playerMarkerToId.get(playerMarker).intValue();
      MemorySegment newBoard = (MemorySegment) withMove.invoke(board, location, playerId);
      return new TicTacToeGameBoard(
          newBoard,
          playerMarkerToId,
          idToPlayerMarker,
          playerIds.getNextId(),
          libTicTacToe,
          cleaner);
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public int dimension() {
    try {
      return (Integer) getDimension.invoke(board);
    } catch (Throwable e) {
      log.log(Level.ERROR, "Error while getting board dimension", e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    int dimension = dimension();
    for (int i = 0; i < dimension; i++) {
      for (int j = 0; j < dimension; j++) {
        int value = getValueAtIndex(i * dimension + j);
        sb.append(idToPlayerMarker.getOrDefault(value, "_"));
        if (j + 1 < dimension) {
          sb.append(" ");
        }
      }
      sb.append("\n");
    }
    return sb.toString();
  }

  @Override
  public String asJsonString() {
    int dimension = dimension();
    StringBuilder json = new StringBuilder();
    json.append("{");
    json.append("\"dimension\":").append(dimension).append(",");
    json.append("\"content\":")
        .append(
            IntStream.range(0, dimension * dimension)
                .mapToObj(i -> getPlayerMarkerAtIndex(i))
                .map(m -> m == null ? "null" : "\"" + m + "\"")
                .collect(Collectors.joining(",", "[", "]")));
    json.append("}");
    return json.toString();
  }

  private void initGameBoardMethods() {
    newGameBoard =
        libTicTacToe
            .find("new_game_board")
            .map(
                m ->
                    linker.downcallHandle(
                        m, FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.JAVA_INT)))
            .orElseThrow(
                () -> new IllegalArgumentException("Unable to find method 'new_game_board'"));
    freeGameBoard =
        libTicTacToe
            .find("free_game_board")
            .map(m -> linker.downcallHandle(m, FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)))
            .orElseThrow(
                () -> new IllegalArgumentException("Unable to find method 'free_game_board'"));
    getDimension =
        libTicTacToe
            .find("get_game_board_dimension")
            .map(
                m ->
                    linker.downcallHandle(
                        m, FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)))
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Unable to find method" + " 'get_game_board_dimension'"));
    withMove =
        libTicTacToe
            .find("get_game_board_with_value_at_index")
            .map(
                m ->
                    linker.downcallHandle(
                        m,
                        FunctionDescriptor.of(
                            ValueLayout.ADDRESS,
                            ValueLayout.ADDRESS,
                            ValueLayout.JAVA_INT,
                            ValueLayout.JAVA_INT)))
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Unable to find method" + " 'get_game_board_with_value_at_index'"));
    getValueAtIndex =
        libTicTacToe
            .find("get_game_board_value_at_index")
            .map(
                m ->
                    linker.downcallHandle(
                        m,
                        FunctionDescriptor.of(
                            ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT)))
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Unable to find method" + " 'get_game_board_value_at_index'"));
    getGameBoardIsFull =
        libTicTacToe
            .find("get_game_board_is_full")
            .map(
                m ->
                    linker.downcallHandle(
                        m, FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS)))
            .orElseThrow(
                () ->
                    new IllegalArgumentException("Unable to find method 'get_game_board_is_full'"));
    getGameBoardHasChain =
        libTicTacToe
            .find("get_game_board_has_chain")
            .map(
                m ->
                    linker.downcallHandle(
                        m,
                        FunctionDescriptor.of(
                            ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS, ValueLayout.JAVA_INT)))
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Unable to find method" + " 'get_game_board_has_chain'"));
  }

  private MemorySegment newGameBoard(int dimension) {
    try {
      return (MemorySegment) newGameBoard.invokeExact(dimension);
    } catch (Throwable e) {
      log.log(Level.ERROR, "Error creating new game board of dimension {0}", dimension, e);
      throw new RuntimeException(e);
    }
  }

  String getPlayerMarkerAtIndex(int index) {
    Integer valueAtIndex = getValueAtIndex(index);
    return idToPlayerMarker.get(valueAtIndex);
  }

  Integer getValueAtIndex(int index) {
    try {
      return (Integer) getValueAtIndex.invoke(board, index);
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  boolean getGameBoardIsFull() {
    try {
      return (Boolean) getGameBoardIsFull.invoke(board);
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  private boolean getGameBoardHasChain(Integer playerId) {
    try {
      return (Boolean) getGameBoardHasChain.invoke(board, playerId);
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }
}
