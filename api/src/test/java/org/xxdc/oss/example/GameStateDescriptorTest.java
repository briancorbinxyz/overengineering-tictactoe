package org.xxdc.oss.example;

import static org.xxdc.oss.example.TestData.createBoardWith;

import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;

public class GameStateDescriptorTest {

  @Test
  public void describe_shouldIncludeKeySectionsAndValues() {
    // Arrange: 3x3 board with some moves
    var board =
        createBoardWith(
            new String[][] {
              {"X", "O", "_"},
              {"_", "X", "_"},
              {"_", "_", "_"}
            });
    var state = new GameState(board, List.of("X", "O"), 1);

    // Act
    var desc = new GameStateDescriptor().describe(state);

    // Assert: basic structure
    Assert.assertTrue(desc.contains("Game State Summary"), "should contain header");
    Assert.assertTrue(
        desc.contains("Players: [0: 'X', 1: 'O']"), "should list players with indices");
    Assert.assertTrue(
        desc.contains("Current player index: 1 (marker='O')"), "should show current player");
    Assert.assertTrue(desc.contains("Board dimension: 3x3"), "should show dimension");

    // Available moves should be non-empty and show coordinates
    Assert.assertTrue(desc.contains("Available moves (count="), "should show moves count");
    Assert.assertTrue(
        desc.contains("(r=1,c=0)") || desc.contains("(r=0,c=2)"),
        "should show some move coordinates");

    // Grid and flat content
    Assert.assertTrue(desc.contains("Board Grid (row-major):"), "should show grid header");
    Assert.assertTrue(desc.contains("Flat content:"), "should show flat content listing");

    // JSON payload of board should be present
    Assert.assertTrue(desc.contains(board.asJsonString()), "should include board JSON payload");
  }
}
