package org.example;

import static org.example.TestData.*;
import static org.testng.Assert.assertEquals;

import java.util.List;
import org.example.algo.Minimax;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MinimaxTest {

  @Test
  public void testMiniMaxShouldPreventOpponentWinningNextMove() {
    var board = new GameBoard[3];
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
              {"X", "_", "_"},
              {"O", "X", "_"},
              {"O", "O", "_"}
            });

    assertEquals(new Minimax(new GameState(board[0], List.of("X", "O"), 1)).bestMove(), 2);
    Assert.assertTrue(board[1].withMove("X", 8).hasChain("X"));
    assertEquals(new Minimax(new GameState(board[2], List.of("X", "O"), 1)).bestMove(), 8);
    assertEquals(new Minimax(new GameState(board[1], List.of("X", "O"), 1)).bestMove(), 8);
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
    assertEquals(new Minimax(new GameState(board[0], List.of("X", "O"), 0)).bestMove(), 2);
    assertEquals(new Minimax(new GameState(board[1], List.of("X", "O"), 0)).bestMove(), 8);
    assertEquals(new Minimax(new GameState(board[2], List.of("X", "O"), 0)).bestMove(), 7);
    assertEquals(new Minimax(new GameState(board[3], List.of("X", "O"), 1)).bestMove(), 5);
    assertEquals(new Minimax(new GameState(board[4], List.of("X", "O"), 1)).bestMove(), 8);
    assertEquals(new Minimax(new GameState(board[5], List.of("X", "O"), 1)).bestMove(), 0);
  }

  @Test
  public void testMinimaxSupportsNonStandardPlayers() {
    var board =
        createBoardWith(
            new String[][] {
              {"♠", "_", "_"},
              {"♣", "♠", "_"},
              {"♣", "_", "_"}
            });
    assertEquals(new Minimax(new GameState(board, List.of("♣", "♠"), 1)).bestMove(), 8);
  }

  @Test
  public void testMinimaxRejectsGamesWithMoreTThanTwoPlayers() {
    var board =
        createBoardWith(
            new String[][] {
              {"♠", "♦", "_"},
              {"♣", "♠", "_"},
              {"♣", "♦", "_"}
            });
    Assert.assertThrows(
        IllegalArgumentException.class,
        () -> new Minimax(new GameState(board, List.of("♣", "♠", "♦"), 1)));
  }
}
