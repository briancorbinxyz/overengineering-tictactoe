package org.example;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import org.example.interop.TicTacToeLibrary;
import org.testng.annotations.Test;

public class TicTacToeLibraryTest {

    @Test public void should_load_library() {
        var ticTacToeLibrary = new TicTacToeLibrary();
        assertNotNull(ticTacToeLibrary);
    }

    @Test public void should_load_game_board() {
        var ticTacToeLibrary = new TicTacToeLibrary();
        var gameBoard = ticTacToeLibrary.newGameBoard(3);
        assertNotNull(gameBoard);
        assertEquals(gameBoard.getDimension(), 3);
    }

}
