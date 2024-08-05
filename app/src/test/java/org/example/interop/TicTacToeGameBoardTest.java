package org.example.interop;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import org.example.GameBoard;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TicTacToeGameBoardTest {

    private TicTacToeLibrary ticTacToeLibrary;

    @BeforeMethod void loadLibrary() {
        ticTacToeLibrary = new TicTacToeLibrary();
    }

    @Test public void should_create_board_with_specified_dimensions() {
        var board = ticTacToeLibrary.newGameBoard(3);
        assertEquals(board.getDimension(), 3);
    }

    @Test public void should_place_playerMarker_at_specified_location() {
        var gameBoard = ticTacToeLibrary.newGameBoard(3);
        var updatedGameBoard = gameBoard.withMove("X", 0);
        assertTrue(updatedGameBoard instanceof TicTacToeGameBoard);
        switch (updatedGameBoard) {
            case TicTacToeGameBoard b:
                assertNotNull(b.getPlayerMarkerAtIndex(0));
                assertEquals(b.getPlayerMarkerAtIndex(0), "X");
                break;
            default:
                throw new AssertionError();
        }
    }

}
