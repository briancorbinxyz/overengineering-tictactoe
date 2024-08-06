package org.example.interop;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

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
        var updatedGameBoard = gameBoard.withMove("X", 4);
        assertTrue(updatedGameBoard instanceof TicTacToeGameBoard);
        switch (updatedGameBoard) {
            case TicTacToeGameBoard b:
                assertNotNull(b.getPlayerMarkerAtIndex(4));
                assertEquals(b.getPlayerMarkerAtIndex(4), "X");
                break;
            default:
                throw new AssertionError();
        }
    }

    @Test public void should_return_null_when_playerMarker_is_not_placed_at_specified_location() {
        var gameBoard = ticTacToeLibrary.newGameBoard(3);
        var updatedGameBoard = gameBoard.withMove("X", 4);
        assertTrue(updatedGameBoard instanceof TicTacToeGameBoard);
        switch (updatedGameBoard) {
            case TicTacToeGameBoard b:
                assertNull(b.getPlayerMarkerAtIndex(5));
                break;
            default:
                throw new AssertionError();
        }
    }

    @Test public void should_validate_move_is_possible() {
        var gameBoard = ticTacToeLibrary.newGameBoard(3);
        var updatedGameBoard = gameBoard.withMove("X", 4);
        switch (updatedGameBoard) {
            case TicTacToeGameBoard b:
                assertTrue(b.isValidMove(0));
                assertFalse(b.isValidMove(4));
                break;
            default:
                throw new AssertionError();
        }
    }

}
