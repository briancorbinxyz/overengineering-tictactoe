package org.example;

import static org.example.TestData.*;
import static org.testng.Assert.assertEquals;

import java.util.List;
import org.example.bot.BotStrategyConfig;
import org.example.bot.MonteCarloTreeSearch;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MonteCarloTreeSearchTest {

  @Test
  public void testMonteCarloTreeSearchShouldPreventOpponentWinningNextMove() {
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

    assertEquals(
        new MonteCarloTreeSearch(new GameState(board[0], List.of("X", "O"), 1)).bestMove(), 2);
    Assert.assertTrue(board[1].withMove("X", 8).hasChain("X"));
    assertEquals(
        new MonteCarloTreeSearch(new GameState(board[1], List.of("X", "O"), 1)).bestMove(), 8);
    assertEquals(
        new MonteCarloTreeSearch(new GameState(board[2], List.of("X", "O"), 1)).bestMove(), 8);
  }

  @Test
  public void testMonteCarloTreeSearchShouldChooseWinningMove() {
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
    assertEquals(
        new MonteCarloTreeSearch(new GameState(board[0], List.of("X", "O"), 0)).bestMove(), 2);
    assertEquals(
        new MonteCarloTreeSearch(new GameState(board[1], List.of("X", "O"), 0)).bestMove(), 8);
    assertEquals(
        new MonteCarloTreeSearch(new GameState(board[2], List.of("X", "O"), 0)).bestMove(), 7);
    assertEquals(
        new MonteCarloTreeSearch(new GameState(board[3], List.of("X", "O"), 1)).bestMove(), 5);
    assertEquals(
        new MonteCarloTreeSearch(new GameState(board[4], List.of("X", "O"), 1)).bestMove(), 8);
    assertEquals(
        new MonteCarloTreeSearch(new GameState(board[5], List.of("X", "O"), 1)).bestMove(), 0);
  }

  @Test
  public void testMonteCarloTreeSearchShouldSupportAMultiPlayerGame() {
    // The player '/' should try to win,
    var board =
        createBoardWith(
            new String[][] {
              {"X", "X", "/"},
              {"O", "_", "/"},
              {"O", "_", "_"}
            });
    var mcts =
        new MonteCarloTreeSearch(
            new GameState(board, List.of("/", "X", "O"), 0),
            BotStrategyConfig.newBuilder().maxTimeMillis(1000L).build());
    assertEquals(mcts.bestMove(), 8);
    // Monte Carlo Tree Search will be able to predict the player '/'
    // and block given sufficient time. Unlike other algo's MCTS doesn't assume
    // perfect moves by the opponent(s), so won't *assume* it doesn't have to block
    // due to the next opponent being expected to block.
    assertEquals(
        new MonteCarloTreeSearch(
                new GameState(board, List.of("X", "/", "O"), 0),
                BotStrategyConfig.newBuilder().maxTimeMillis(1_000L).build())
            .bestMove(),
        8);
    assertEquals(
        new MonteCarloTreeSearch(
                new GameState(board, List.of("X", "O", "/"), 0),
                BotStrategyConfig.newBuilder().maxTimeMillis(1_000L).build())
            .bestMove(),
        8);
    assertEquals(
        new MonteCarloTreeSearch(
                new GameState(board, List.of("O", "/", "X"), 0),
                BotStrategyConfig.newBuilder().maxTimeMillis(1_000L).build())
            .bestMove(),
        8);
  }
}
