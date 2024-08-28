package org.xxdc.oss.example.interop;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import org.testng.annotations.Test;

import org.xxdc.oss.example.interop.TicTacToeLibrary;

public class TicTacToeLibraryTest {

  @Test
  public void should_load_library() {
    var ticTacToeLibrary = new TicTacToeLibrary();
    assertNotNull(ticTacToeLibrary);
  }

  @Test
  public void should_load_game_board_using_library() {
    var ticTacToeLibrary = new TicTacToeLibrary();
    var gameBoard = ticTacToeLibrary.newGameBoard(3);
    assertNotNull(gameBoard);
    assertEquals(gameBoard.dimension(), 3);
  }
}
