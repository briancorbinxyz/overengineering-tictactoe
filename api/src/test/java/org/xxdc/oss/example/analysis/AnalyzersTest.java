package org.xxdc.oss.example.analysis;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.xxdc.oss.example.TestData.createBoardWith;
import static org.xxdc.oss.example.analysis.Analyzers.strategicTurningPoints;

import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.Test;
import org.xxdc.oss.example.GameBoard;
import org.xxdc.oss.example.GameState;

public class AnalyzersTest {

  @Test
  public void should_discover_strategic_turning_point_center_square_control() {
    var states = new ArrayList<GameState>(2);
    var gameState = new GameState(emptyBoard(), List.of("X", "O"), 0);
    states.add(gameState); // Initial state
    states.add(gameState.afterPlayerMoves(4)); // 'X' to center square

    var strategicTurningPoints = states.stream().gather(strategicTurningPoints()).toList();

    assertEquals(strategicTurningPoints.size(), 1);
    assertTrue(
        strategicTurningPoints.getFirst() instanceof StrategicTurningPoint.CenterSquareControl);
  }

  @Test
  public void should_not_incorrectly_discover_strategic_turning_point_center_square_control() {
    var states = new ArrayList<GameState>(2);
    var gameState = new GameState(emptyBoard(), List.of("X", "O"), 0);
    states.add(gameState); // Initial state
    states.add(gameState.afterPlayerMoves(5)); // 'X' to non-center square

    var strategicTurningPoints = states.stream().gather(strategicTurningPoints()).toList();

    assertTrue(strategicTurningPoints.isEmpty());
  }

  @Test
  public void should_skip_center_control_when_chain_length_less_than_dimension() {
    // 5x5 board with chainLength=3: center control is NOT a turning point
    var board = GameBoard.withDimension(5, 3);
    var states = new ArrayList<GameState>(2);
    var gameState = new GameState(board, List.of("X", "O"), 0);
    states.add(gameState);
    states.add(gameState.afterPlayerMoves(12)); // 'X' to center of 5x5

    var strategicTurningPoints = states.stream().gather(strategicTurningPoints()).toList();

    assertTrue(strategicTurningPoints.isEmpty());
  }

  @Test
  public void should_detect_game_won_with_custom_chain_length() {
    // 5x5 board with chainLength=3: X wins with 3-in-a-row
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
    var states = new ArrayList<GameState>(2);
    var gameState = new GameState(board, List.of("X", "O"), 0);
    states.add(gameState);
    states.add(gameState.afterPlayerMoves(2)); // X completes 3-in-a-row

    var strategicTurningPoints = states.stream().gather(strategicTurningPoints()).toList();

    assertEquals(strategicTurningPoints.size(), 1);
    assertTrue(strategicTurningPoints.getFirst() instanceof StrategicTurningPoint.GameWon);
  }

  private GameBoard emptyBoard() {
    return createBoardWith(
        new String[][] {
          {"_", "_", "_"}, // 0, 1, 2
          {"_", "_", "_"}, // 3, 4, 5
          {"_", "_", "_"} // 6, 7, 8
        });
  }
}
