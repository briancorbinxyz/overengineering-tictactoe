package org.example;

import static org.example.TestData.*;

import org.example.algo.Minimax;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MinimaxTest {

  @Test
  public void testMiniMaxShouldPreventOpponentWinningNextMove() {
    var board =
        createBoardWith(
            new String[][] {
              {"X", "X", "_"},
              {"O", "_", "_"},
              {"O", "_", "_"}
            });
    var board2 =
        createBoardWith(
            new String[][] {
              {"X", "_", "_"},
              {"O", "X", "_"},
              {"O", "_", "_"}
            });
    var board3 =
        createBoardWith(
            new String[][] {
              {"X", "_", "_"},
              {"O", "X", "_"},
              {"O", "O", "_"}
            });

    Assert.assertEquals(new Minimax("O", board).bestMove(), 2);
    Assert.assertTrue(board2.withMove("X", 8).hasChain("X"));
    Assert.assertEquals(new Minimax("X", board3).bestMove(), 8);
    Assert.assertEquals(new Minimax("O", board2).bestMove(), 8);
  }

  @Test
  public void testMiniMaxShouldChooseWinningMove() {
    var board = new GameBoard[6];
    board[0] =
        createBoardWith(
            new String[][] {
              {"X", "X", "_"},
              {"O", "_", "_"},
              {"O", "_", "_"}
            });
    board[1] =
        createBoardWith(
            new String[][] {
              {"X", "_", "_"},
              {"O", "X", "_"},
              {"O", "_", "_"}
            });
    board[2] =
        createBoardWith(
            new String[][] {
              {"_", "X", "_"},
              {"O", "X", "_"},
              {"O", "_", "_"}
            });
    board[3] =
        createBoardWith(
            new String[][] {
              {"X", "X", "_"},
              {"O", "O", "_"},
              {"X", "_", "_"}
            });
    board[4] =
        createBoardWith(
            new String[][] {
              {"O", "_", "_"},
              {"X", "O", "_"},
              {"X", "_", "_"}
            });
    board[5] =
        createBoardWith(
            new String[][] {
              {"_", "X", "_"},
              {"O", "X", "_"},
              {"O", "_", "_"}
            });
    Assert.assertEquals(new Minimax("X", board[0]).bestMove(), 2);
    Assert.assertEquals(new Minimax("X", board[1]).bestMove(), 8);
    Assert.assertEquals(new Minimax("X", board[2]).bestMove(), 7);
    Assert.assertEquals(new Minimax("O", board[3]).bestMove(), 5);
    Assert.assertEquals(new Minimax("O", board[4]).bestMove(), 8);
    Assert.assertEquals(new Minimax("O", board[5]).bestMove(), 0);
  }
}
