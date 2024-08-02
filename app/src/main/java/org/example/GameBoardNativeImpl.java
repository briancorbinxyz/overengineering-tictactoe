package org.example;

import java.lang.System.Logger;
import java.lang.invoke.MethodHandles;

import org.example.interop.TicTacToeLibrary;

/**
 * Implements the GameBoard interface using a native library. This class manages
 * the lifetime of the
 * native library resources and provides methods to interact with the native
 * library.
 */
public class GameBoardNativeImpl implements GameBoard {

    // Bound to GameBoardNativeImpl lifetime, Not explicitly closeable, Accessible
    // from any thread
    // https://docs.oracle.com/en%2Fjava%2Fjavase%2F22%2Fdocs%2Fapi%2F%2F/java.base/java/lang/foreign/Arena.html
    static final Logger log = System.getLogger(MethodHandles.lookup().lookupClass().getName());

    private final TicTacToeLibrary library;

    public GameBoardNativeImpl() {
        library = new TicTacToeLibrary();
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toString'");
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDimension'");
    }

    @Override
    public String asJsonString() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'asJsonString'");
    }
}
