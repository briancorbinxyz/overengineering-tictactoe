package org.xxdc.oss.example;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.xxdc.oss.example.TestData.*;

import java.util.List;
import java.util.UUID;
import org.testng.Assert;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import org.xxdc.oss.example.bot.AlphaBeta;
import org.xxdc.oss.example.bot.BotStrategyConfig;

public class AlphaBetaTest {

  @Test
  public void testAlphaBetaShouldPreventOpponentWinningNextMove() {
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
        new AlphaBeta(new GameState(UUID.randomUUID(), board[0], List.of("X", "O"), 1)).bestMove(),
        2);
    Assert.assertTrue(board[1].withMove("X", 8).hasChain("X"));
    assertEquals(
        new AlphaBeta(new GameState(UUID.randomUUID(), board[2], List.of("X", "O"), 1)).bestMove(),
        8);
    assertEquals(
        new AlphaBeta(new GameState(UUID.randomUUID(), board[1], List.of("X", "O"), 1)).bestMove(),
        8);
  }

  @Test
  public void testAlphaBetaShouldChooseWinningMove() {
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
        new AlphaBeta(new GameState(UUID.randomUUID(), board[0], List.of("X", "O"), 0)).bestMove(),
        2);
    assertEquals(
        new AlphaBeta(new GameState(UUID.randomUUID(), board[1], List.of("X", "O"), 0)).bestMove(),
        8);
    assertEquals(
        new AlphaBeta(new GameState(UUID.randomUUID(), board[2], List.of("X", "O"), 0)).bestMove(),
        7);
    assertEquals(
        new AlphaBeta(new GameState(UUID.randomUUID(), board[3], List.of("X", "O"), 1)).bestMove(),
        5);
    assertEquals(
        new AlphaBeta(new GameState(UUID.randomUUID(), board[4], List.of("X", "O"), 1)).bestMove(),
        8);
    assertEquals(
        new AlphaBeta(new GameState(UUID.randomUUID(), board[5], List.of("X", "O"), 1)).bestMove(),
        0);
  }

  @Test
  public void testAlphaBetaSupportsNonStandardPlayers() {
    var board =
        createBoardWith(
            new String[][] {
              {"♠", "_", "_"},
              {"♣", "♠", "_"},
              {"♣", "_", "_"}
            });
    assertEquals(
        new AlphaBeta(new GameState(UUID.randomUUID(), board, List.of("♣", "♠"), 1)).bestMove(), 8);
  }

  @Test
  public void testAlphaBetaRejectsGamesWithMoreTThanTwoPlayers() {
    var board =
        createBoardWith(
            new String[][] {
              {"♠", "♦", "_"},
              {"♣", "♠", "_"},
              {"♣", "♦", "_"}
            });
    Assert.assertThrows(
        IllegalArgumentException.class,
        () -> new AlphaBeta(new GameState(UUID.randomUUID(), board, List.of("♣", "♠", "♦"), 1)));
  }

  @Test
  @Ignore("This is a slow test")
  public void testAlphaBetaCanPlayLargeGames() {
    var board =
        createBoardWith(
            new String[][] {
              {"♣", "_", "_", "_"},
              {"♣", "♠", "_", "_"},
              {"♣", "♠", "_", "_"},
              {"_", "♠", "_", "_"}
            });
    assertEquals(
        new AlphaBeta(new GameState(UUID.randomUUID(), board, List.of("♣", "♠"), 1)).bestMove(), 1);
  }

  @Ignore(
      "This test is slow, but it's a good test to run to make sure that the algorithm can handle"
          + " larger games.")
  public void testAlphaBetaCanPlayLargerGamesWhenDepthLimited() {
    // This test is a bit slow, but it's a good test to run to make sure that the algorithm can
    // handle larger games.
    var board =
        createBoardWith(
            new String[][] {
              {"♠", "_", "_", "_"},
              {"♣", "_", "_", "_"},
              {"_", "_", "_", "_"},
              {"_", "_", "_", "_"}
            });
    GameState state = new GameState(UUID.randomUUID(), board, List.of("♣", "♠"), 1);
    BotStrategyConfig config = BotStrategyConfig.newBuilder().maxDepth(3).build();
    assertTrue(new AlphaBeta(state, config).bestMove() > 0);
  }

  @Test
  public void testAlphaBetaCanPlayBlogConfiguration() {
    var board =
        createBoardWith(
            new String[][] {
              {"X", "_", "O"},
              {"O", "_", "_"},
              {"O", "X", "X"}
            });
    assertEquals(
        new AlphaBeta(new GameState(UUID.randomUUID(), board, List.of("O", "X"), 0)).bestMove(), 4);
  }

  @Test
  public void testAlphaBetaCanPlayBlogConfigurationTwo() {
    var board =
        createBoardWith(
            new String[][] {
              {"O", "O", "_"},
              {"X", "X", "_"},
              {"_", "_", "_"}
            });
    assertEquals(
        new AlphaBeta(new GameState(UUID.randomUUID(), board, List.of("O", "X"), 0)).bestMove(), 2);
  }
}
