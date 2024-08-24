package org.example;

import static org.example.TestData.*;
import static org.testng.Assert.assertNotNull;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.invoke.MethodHandles;
import org.testng.Assert;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

public class GameBoardNativeImplTest {

  private static final Logger log =
      System.getLogger(MethodHandles.lookup().lookupClass().getName());

  @Test
  public void should_load_library() {
    printEnvironmentVariables();
    printSystemProperties();
    GameBoard gameBoard = new GameBoardNativeImpl();
    assertNotNull(gameBoard);
  }

  @Test
  public void should_correctly_determine_winner() {
    GameBoard[] board = new GameBoard[5];
    board[0] =
        createBoardWith(
            new String[][] {
              {"X", "X", "X"},
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
              {"X", "X", "_"},
              {"O", "X", "X"},
              {"O", "O", "_"}
            });
    board[3] =
        createBoardWith(
            new String[][] {
              {"X", "O", "X"},
              {"O", "O", "_"},
              {"_", "X", "X"}
            });
    board[3] =
        createBoardWith(
            new String[][] {
              {"X", "X", "O"},
              {"O", "X", "O"},
              {"O", "_", "X"}
            });
    board[4] =
        createBoardWith(
            new String[][] {
              {"X", "X", "O"},
              {"O", "O", "_"},
              {"_", "X", "_"}
            });

    Assert.assertTrue(board[0].hasChain("X"));
    Assert.assertFalse(board[0].hasChain("O"));
    Assert.assertTrue(board[1].withMove("X", 8).hasChain("X"));
    Assert.assertTrue(board[2].withMove("O", 8).hasChain("O"));
    Assert.assertTrue(board[3].hasChain("X"));
    Assert.assertFalse(board[3].hasChain("O"));
    Assert.assertFalse(board[4].hasChain("O"));
  }

  @Test
  @Ignore("This test is for debugging and memory profiling only")
  public void should_clean_up_native_resources() {
    long idx = 0;
    while (true) {
      GameBoard gameBoard = new GameBoardNativeImpl();
      gameBoard = null;
      if (idx++ % 100 == 0) {
        System.gc();
        log.log(Level.INFO, "GC'd " + idx / 100 + " times at " + idx + " iterations");
      }
    }
  }

  private void printSystemProperties() {
    System.getProperties().forEach((k, v) -> log.log(Level.INFO, k + " = " + v));
  }

  private void printEnvironmentVariables() {
    System.getenv().forEach((k, v) -> log.log(Level.INFO, k + " = " + v));
  }
}
