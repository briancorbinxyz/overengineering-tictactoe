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

import org.example.GameBoard;

class TicTacToeGameBoard implements GameBoard {

    private static final Logger log = System.getLogger(MethodHandles.lookup().lookupClass().getName());

    private final MemorySegment board;

    private final Linker linker = Linker.nativeLinker();

    private final SymbolLookup libTicTacToe;

    private final Cleaner cleaner = Cleaner.create();

    private final Cleaner.Cleanable cleanable;

    private MethodHandle newGameBoard;
    private MethodHandle freeGameBoard;
    private MethodHandle getDimension;

    public TicTacToeGameBoard(int dimension, SymbolLookup libTicTacToe) {
        this.libTicTacToe = libTicTacToe;
        this.initGameBoardMethods();
        this.cleanable = cleaner.register(this, () -> {
            log.log(Level.DEBUG, "Cleaning up native resources for TicTacToeGameBoard");
            freeGameBoard();
        });
        this.board = newGameBoard(dimension);
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'withMove'");
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

}