package org.xxdc.oss.example;

import static org.testng.Assert.assertEquals;
import static org.xxdc.oss.example.TestData.*;

import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xxdc.oss.example.bot.Paranoid;

public class ParanoidTest {

  @Test
  public void testParanoidShouldPreventOpponentWinningNextMove() {
    // Should behave similarly to minimax for a two-player game
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

    Assert.assertTrue(board[0].withMove("X", 2).hasChain("X"));
    assertEquals(new Paranoid(new GameState(board[0], List.of("X", "O"), 0)).bestMove(), 2);
    // Paranoid may not choose the winning move if it will lose next turn
    // assertEquals(new Paranoid(new GameState(board[0], List.of("X", "O"), 1)).bestMove(), 2);
    Assert.assertTrue(board[1].withMove("X", 8).hasChain("X"));
    assertEquals(new Paranoid(new GameState(board[1], List.of("X", "O"), 1)).bestMove(), 8);
    assertEquals(new Paranoid(new GameState(board[2], List.of("X", "O"), 1)).bestMove(), 8);
  }

  @Test
  public void testParanoidShouldChooseWinningMove() {
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
    assertEquals(new Paranoid(new GameState(board[0], List.of("X", "O"), 0)).bestMove(), 2);
    assertEquals(new Paranoid(new GameState(board[1], List.of("X", "O"), 0)).bestMove(), 8);
    assertEquals(new Paranoid(new GameState(board[2], List.of("X", "O"), 0)).bestMove(), 7);
    assertEquals(new Paranoid(new GameState(board[3], List.of("X", "O"), 1)).bestMove(), 5);
    assertEquals(new Paranoid(new GameState(board[4], List.of("X", "O"), 1)).bestMove(), 8);
    assertEquals(new Paranoid(new GameState(board[5], List.of("X", "O"), 1)).bestMove(), 0);
  }

  @Test
  public void testParanoidShouldSupportAMultiPlayerGame() {
    // The player '/' should try to win,
    var board =
        createBoardWith(
            new String[][] {
              {"X", "X", "/"},
              {"O", "_", "/"},
              {"O", "_", "_"}
            });
    assertEquals(new Paranoid(new GameState(board, List.of("/", "X", "O"), 0)).bestMove(), 8);
    assertEquals(new Paranoid(new GameState(board, List.of("X", "/", "O"), 0)).bestMove(), 8);
    assertEquals(new Paranoid(new GameState(board, List.of("O", "/", "X"), 0)).bestMove(), 8);
  }

  @Test
  public void testParanoidShouldSupportAMultiPlayerGameTwo() {
    // The player '/' should try to win,
    var board =
        createBoardWith(
            new String[][] {
              {"O", "/", "/"},
              {"X", "_", "X"},
              {"O", "_", "_"}
            });
    assertEquals(new Paranoid(new GameState(board, List.of("O", "X", "/"), 0)).bestMove(), 4);
  }
}
