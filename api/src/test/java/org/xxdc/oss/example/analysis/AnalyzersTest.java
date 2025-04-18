package org.xxdc.oss.example.analysis;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.xxdc.oss.example.TestData.createBoardWith;
import static org.xxdc.oss.example.analysis.Analyzers.strategicTurningPoints;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.testng.annotations.Test;
import org.xxdc.oss.example.GameBoard;
import org.xxdc.oss.example.GameState;

public class AnalyzersTest {

  @Test
  public void should_discover_strategic_turning_point_center_square_control() {
    var states = new ArrayList<GameState>(2);
    var gameState = new GameState(UUID.randomUUID(), emptyBoard(), List.of("X", "O"), 0);
    states.add(gameState); // Initial state
    states.add(gameState.afterPlayerMoves(4)); // 'X' to center square

    var strategicTurningPoints = states.stream().gather(strategicTurningPoints()).toList();

    assertEquals(strategicTurningPoints.size(), 1);
    assertTrue(strategicTurningPoints.get(0) instanceof StrategicTurningPoint.CenterSquareControl);
  }

  @Test
  public void should_not_incorrectly_discover_strategic_turning_point_center_square_control() {
    var states = new ArrayList<GameState>(2);
    var gameState = new GameState(UUID.randomUUID(), emptyBoard(), List.of("X", "O"), 0);
    states.add(gameState); // Initial state
    states.add(gameState.afterPlayerMoves(5)); // 'X' to non-center square

    var strategicTurningPoints = states.stream().gather(strategicTurningPoints()).toList();

    assertTrue(strategicTurningPoints.isEmpty());
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
