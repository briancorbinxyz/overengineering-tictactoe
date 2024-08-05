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
import java.util.concurrent.atomic.AtomicInteger;

import org.example.GameBoard;

class TicTacToeGameBoard implements GameBoard {

    private static final Logger log = System.getLogger(MethodHandles.lookup().lookupClass().getName());

    private final MemorySegment board;

    private final Linker linker = Linker.nativeLinker();

    private final SymbolLookup libTicTacToe;

    private final Cleaner cleaner = Cleaner.create();

    private final AtomicInteger nextId;

    private final Map<String, Integer> playerMarkerToId;
    private final Map<Integer, String> idToPlayerMarker;

    private final Cleaner.Cleanable cleanable;

    private MethodHandle newGameBoard;
    private MethodHandle freeGameBoard;
    private MethodHandle getDimension;
    private MethodHandle withMove;
    private MethodHandle getValueAtIndex;

    public TicTacToeGameBoard(int dimension, SymbolLookup libTicTacToe) {
        this.libTicTacToe = libTicTacToe;
        this.playerMarkerToId = new HashMap<>();
        this.idToPlayerMarker = new HashMap<>();
        this.nextId = new AtomicInteger(1);
        this.initGameBoardMethods();
        this.cleanable = cleaner.register(this, () -> {
            log.log(Level.DEBUG, "Cleaning up native resources for TicTacToeGameBoard");
            freeGameBoard();
        });
        this.board = newGameBoard(dimension);
    }

    TicTacToeGameBoard(MemorySegment board, Map<String, Integer> playerMarkerToId,
            Map<Integer, String> idToPlayerMarker, int initialValue, SymbolLookup libTicTacToe) {
        this.libTicTacToe = libTicTacToe;
        this.playerMarkerToId = new HashMap<>(playerMarkerToId);
        this.idToPlayerMarker = new HashMap<>(idToPlayerMarker);
        this.nextId = new AtomicInteger(initialValue);
        this.initGameBoardMethods();
        this.board = board;
        this.cleanable = cleaner.register(this, () -> {
            log.log(Level.DEBUG, "Cleaning up native resources for TicTacToeGameBoard");
            freeGameBoard();
        });
    }

    @Override
    public boolean isValidMove(int location) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isValidMove'");
    }

    @Override
    public boolean hasChain(String playerMarker) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'hasChain'");
    }

    @Override
    public boolean hasMovesAvailable() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'hasMovesAvailable'");
    }

    @Override
    public GameBoard withMove(String playerMarker, int location) {
        if (!playerMarkerToId.containsKey(playerMarker)) {
            int id = nextId.getAndIncrement();
            playerMarkerToId.put(playerMarker, id);
            idToPlayerMarker.put(id, playerMarker);
        }

        try {
            MemorySegment newBoard = (MemorySegment) withMove.invoke(board,
                    playerMarkerToId.get(playerMarker).intValue(), location);
            return new TicTacToeGameBoard(newBoard, playerMarkerToId, idToPlayerMarker, nextId.get(), libTicTacToe);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getDimension() {
        try {
            return (Integer) getDimension.invoke(board);
        } catch (Throwable e) {
            log.log(Level.ERROR, "Error while getting board dimension", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public String asJsonString() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'asJsonString'");
    }

    private void initGameBoardMethods() {
        newGameBoard = libTicTacToe.find("new_game_board")
                .map(m -> linker.downcallHandle(m, FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.JAVA_INT)))
                .orElseThrow(() -> new IllegalArgumentException("Unable to find method 'new_game_board'"));
        freeGameBoard = libTicTacToe.find("free_game_board")
                .map(m -> linker.downcallHandle(m, FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)))
                .orElseThrow(() -> new IllegalArgumentException("Unable to find method 'free_game_board'"));
        getDimension = libTicTacToe.find("get_game_board_dimension")
                .map(m -> linker.downcallHandle(m, FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)))
                .orElseThrow(() -> new IllegalArgumentException("Unable to find method 'get_game_board_dimension'"));
        withMove = libTicTacToe.find("get_game_board_with_value_at_index")
                .map(m -> linker.downcallHandle(m,
                        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT,
                                ValueLayout.JAVA_INT)))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unable to find method 'get_game_board_with_value_at_index'"));
        getValueAtIndex = libTicTacToe.find("get_game_board_value_at_index")
                .map(m -> linker.downcallHandle(m,
                        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT)))
                .orElseThrow(
                        () -> new IllegalArgumentException("Unable to find method 'get_game_board_value_at_index'"));
    }

    private MemorySegment newGameBoard(int dimension) {
        try {
            return (MemorySegment) newGameBoard.invokeExact(dimension);
        } catch (Throwable e) {
            log.log(Level.ERROR, "Error creating new game board of dimension {0}", dimension, e);
            throw new RuntimeException(e);
        }
    }

    private void freeGameBoard() {
        try {
            freeGameBoard.invokeExact(board);
        } catch (Throwable e) {
            log.log(Level.ERROR, "Error cleaning up game board", e);
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

}