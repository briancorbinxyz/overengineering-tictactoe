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

  @Test
  public void testCanCorrectlyIdentifyOccupiedSquares() {
    var gameBoard =
        createBoardWith(
            new String[][] {
              {"X", "O", "X"},
              {"O", "X", "O"},
              {"X", "O", "X"}
            });
    assertTrue(gameBoard.hasPlayer("X", 0));
    assertTrue(gameBoard.hasPlayer("O", 1));
    assertTrue(gameBoard.hasPlayer("X", 2));
    assertTrue(gameBoard.hasPlayer("O", 3));
    assertTrue(gameBoard.hasPlayer("X", 4));
    assertTrue(gameBoard.hasPlayer("O", 5));
    assertTrue(gameBoard.hasPlayer("X", 6));
    assertTrue(gameBoard.hasPlayer("O", 7));
    assertTrue(gameBoard.hasPlayer("X", 8));
  }

  @Test
  public void doesNotIncorrectlyIdentifyOccupiedSquares() {
    var gameBoard =
        createBoardWith(
            new String[][] {
              {"_", "O", "X"},
              {"O", "X", "O"},
              {"X", "O", "X"}
            });
    assertFalse(gameBoard.hasPlayer("X", 0));
    assertFalse(gameBoard.hasPlayer("X", 1));
    assertFalse(gameBoard.hasPlayer("O", 2));
    assertFalse(gameBoard.hasPlayer("X", 3));
    assertFalse(gameBoard.hasPlayer("O", 4));
    assertFalse(gameBoard.hasPlayer("X", 5));
    assertFalse(gameBoard.hasPlayer("O", 6));
    assertFalse(gameBoard.hasPlayer("X", 7));
    assertFalse(gameBoard.hasPlayer("O", 8));
  }

  // --- Configurable chain length tests (T008, T010, T011, T012, T017) ---

  // T010: Default chainLength equals dimension
  @Test
  public void testDefaultChainLengthEqualsDimension() {
    var board = GameBoard.withDimension(3);
    assertEquals(board.chainLength(), 3);
    var board5 = GameBoard.withDimension(5);
    assertEquals(board5.chainLength(), 5);
  }

  // T008(a): 3x3 with default chainLength=3 detects wins (backward compat)
  @Test
  public void testDefaultChainLength3x3DetectsRowWin() {
    var board =
        createBoardWith(
            new String[][] {
              {"X", "X", "X"},
              {"O", "O", "_"},
              {"_", "_", "_"}
            });
    assertTrue(board.hasChain("X"));
    assertFalse(board.hasChain("O"));
  }

  @Test
  public void testDefaultChainLength3x3DetectsColumnWin() {
    var board =
        createBoardWith(
            new String[][] {
              {"X", "O", "_"},
              {"X", "O", "_"},
              {"X", "_", "_"}
            });
    assertTrue(board.hasChain("X"));
  }

  @Test
  public void testDefaultChainLength3x3DetectsDiagonalWin() {
    var board =
        createBoardWith(
            new String[][] {
              {"X", "O", "_"},
              {"O", "X", "_"},
              {"_", "_", "X"}
            });
    assertTrue(board.hasChain("X"));
  }

  // T008(b): 5x5 with chainLength=3 detects 3-in-a-row
  // T011: Same scenario for US2
  @Test
  public void testCustomChainLength3On5x5DetectsRowWin() {
    var board =
        createBoardWith(
            new String[][] {
              {"X", "X", "X", "_", "_"},
              {"O", "O", "_", "_", "_"},
              {"_", "_", "_", "_", "_"},
              {"_", "_", "_", "_", "_"},
              {"_", "_", "_", "_", "_"}
            },
            3);
    assertTrue(board.hasChain("X"));
  }

  @Test
  public void testCustomChainLength3On5x5DetectsColumnWin() {
    var board =
        createBoardWith(
            new String[][] {
              {"X", "O", "_", "_", "_"},
              {"X", "O", "_", "_", "_"},
              {"X", "_", "_", "_", "_"},
              {"_", "_", "_", "_", "_"},
              {"_", "_", "_", "_", "_"}
            },
            3);
    assertTrue(board.hasChain("X"));
  }

  @Test
  public void testCustomChainLength3On5x5DetectsDiagonalWin() {
    var board =
        createBoardWith(
            new String[][] {
              {"X", "O", "_", "_", "_"},
              {"O", "X", "_", "_", "_"},
              {"_", "_", "X", "_", "_"},
              {"_", "_", "_", "_", "_"},
              {"_", "_", "_", "_", "_"}
            },
            3);
    assertTrue(board.hasChain("X"));
  }

  @Test
  public void testCustomChainLength3On5x5DetectsAntiDiagonalWin() {
    var board =
        createBoardWith(
            new String[][] {
              {"_", "_", "X", "_", "_"},
              {"_", "X", "_", "_", "_"},
              {"X", "_", "_", "_", "_"},
              {"_", "_", "_", "_", "_"},
              {"_", "_", "_", "_", "_"}
            },
            3);
    assertTrue(board.hasChain("X"));
  }

  // T008(c): 5x5 with chainLength=3, 2-in-a-row is NOT a win
  @Test
  public void testCustomChainLength3On5x5DoesNotDetect2InARow() {
    var board =
        createBoardWith(
            new String[][] {
              {"X", "X", "_", "_", "_"},
              {"O", "O", "_", "_", "_"},
              {"_", "_", "_", "_", "_"},
              {"_", "_", "_", "_", "_"},
              {"_", "_", "_", "_", "_"}
            },
            3);
    assertFalse(board.hasChain("X"));
  }

  // T008(d): chainLength > dimension throws
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testChainLengthGreaterThanDimensionThrows() {
    GameBoard.withDimension(3, 4);
  }

  // T008(e): chainLength < 2 throws
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testChainLengthLessThan2Throws() {
    GameBoard.withDimension(3, 1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testChainLengthZeroThrows() {
    GameBoard.withDimension(3, 0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testChainLengthNegativeThrows() {
    GameBoard.withDimension(3, -1);
  }

  // T008(f): withMove preserves chainLength
  @Test
  public void testWithMovePreservesChainLength() {
    var board = GameBoard.withDimension(5, 3);
    assertEquals(board.chainLength(), 3);
    var boardAfterMove = board.withMove("X", 0);
    assertEquals(boardAfterMove.chainLength(), 3);
  }

  // T008(h): chainLength preserved after multiple withMove calls (FR-010 immutability)
  @Test
  public void testChainLengthPreservedAfterMultipleMoves() {
    var board = GameBoard.withDimension(5, 3);
    board = board.withMove("X", 0);
    board = board.withMove("O", 1);
    board = board.withMove("X", 6);
    assertEquals(board.chainLength(), 3);
  }

  // T008(g): hasWinnableChain returns false when no K-length window remains
  @Test
  public void testHasWinnableChainReturnsFalseWhenBlocked() {
    // 3x3 board with chainLength=3, all rows/cols/diags blocked
    var board =
        createBoardWith(
            new String[][] {
              {"X", "O", "X"},
              {"X", "X", "O"},
              {"O", "X", "O"}
            });
    assertFalse(board.hasChain("X"));
    assertFalse(board.hasChain("O"));
    assertFalse(board.hasWinnableChain());
  }

  @Test
  public void testHasWinnableChainReturnsTrueWhenWindowOpen() {
    var board =
        createBoardWith(
            new String[][] {
              {"X", "_", "_"},
              {"_", "_", "_"},
              {"_", "_", "_"}
            });
    assertTrue(board.hasWinnableChain());
  }

  // T012: 5x5 with chainLength=5 behaves same as default
  @Test
  public void testChainLength5On5x5EqualsDefault() {
    var boardDefault =
        createBoardWith(
            new String[][] {
              {"X", "X", "X", "X", "X"},
              {"O", "O", "O", "O", "_"},
              {"_", "_", "_", "_", "_"},
              {"_", "_", "_", "_", "_"},
              {"_", "_", "_", "_", "_"}
            });
    var boardExplicit =
        createBoardWith(
            new String[][] {
              {"X", "X", "X", "X", "X"},
              {"O", "O", "O", "O", "_"},
              {"_", "_", "_", "_", "_"},
              {"_", "_", "_", "_", "_"},
              {"_", "_", "_", "_", "_"}
            },
            5);
    assertEquals(boardDefault.hasChain("X"), boardExplicit.hasChain("X"));
    assertTrue(boardExplicit.hasChain("X"));
  }

  // T017: chainLength=2 on 3x3 succeeds (valid edge case)
  @Test
  public void testChainLength2On3x3IsValid() {
    var board = GameBoard.withDimension(3, 2);
    assertEquals(board.chainLength(), 2);
    var boardWithMoves = board.withMove("X", 0).withMove("X", 1);
    assertTrue(boardWithMoves.hasChain("X"));
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
