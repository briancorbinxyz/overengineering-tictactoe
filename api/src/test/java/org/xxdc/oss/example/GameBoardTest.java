package org.xxdc.oss.example;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.xxdc.oss.example.TestData.*;

import org.testng.annotations.Test;

public class GameBoardTest {

  @Test
  public void testDefaultGameBoardSizeIs3x3() {
    var gameBoard = GameBoard.withDimension(3);
    assertEquals(gameBoard.dimension(), 3);
  }

  @Test
  public void testNewGameBoardHasAllMovesAvailable() {
    GameBoard gameBoard = GameBoard.withDimension(3);
    assertEquals(gameBoard.availableMoves().size(), 9);
  }

  @Test
  public void testPopulatedGameBoardHasCorrectNumberOfAvailableMoves() {
    var gameBoard =
        createBoardWith(
            new String[][] {
              {"_", "O", "X"},
              {"O", "X", "O"},
              {"X", "O", "X"}
            });
    assertEquals(gameBoard.availableMoves().size(), 1);
    assertEquals(gameBoard.availableMoves().getFirst(), 0);
  }

  @Test
  public void testFullGameBoardHasNoAvailableMoves() {
    var gameBoard =
        createBoardWith(
            new String[][] {
              {"X", "O", "X"},
              {"O", "X", "O"},
              {"X", "O", "X"}
            });
    assertEquals(gameBoard.availableMoves().size(), 0);
  }

  // _ X O
  // O X O
  // X O X
  @Test
  public void testPopulatedGameBoardHasCorrectNumberOfAvailableMoves2() {
    var gameBoard =
        createBoardWith(
            new String[][] {
              {"_", "X", "O"},
              {"O", "X", "O"},
              {"X", "O", "X"}
            });
    assertEquals(gameBoard.availableMoves().size(), 1);
    assertEquals(gameBoard.availableMoves().getFirst(), 0);
    assertTrue(gameBoard.hasMovesAvailable());
    assertFalse(gameBoard.withMove("X", 0).hasMovesAvailable());
    assertFalse(gameBoard.withMove("O", 0).hasMovesAvailable());
  }
}
