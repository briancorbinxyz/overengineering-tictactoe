package org.xxdc.oss.example;

import static org.xxdc.oss.example.TestData.createBoardWith;

import java.util.List;
import java.util.UUID;
import org.testng.Assert;
import org.testng.annotations.Test;

public class GameStateTest {

  @Test
  public void testGameStateShouldCreateJsonString() {
    var gameBoard =
        createBoardWith(
            new String[][] {
              {"X", "O", "_"},
              {"_", "X", "_"},
              {"_", "_", "_"}
            });
    var gameId = UUID.fromString("00000000-0000-0000-0000-000000000000");
    var gameState = new GameState(gameId, gameBoard, List.of("X", "O"), 1);
    var json = gameState.asJsonString();
    var expectedJson =
        "{\"gameId\":\"00000000-0000-0000-0000-000000000000\",\"playerMarkers\":[\"X\",\"O\"],\"currentPlayerIndex\":1,\"board\":{\"dimension\":3,\"content\":[\"X\",\"O\",null,null,\"X\",null,null,null,null]}}";
    Assert.assertEquals(json, expectedJson);
  }

  @Test
  public void shouldCorrectlyUpdateStateAfterMove() {
    var gameBoard =
        createBoardWith(
            new String[][] {
              {"X", "O", "_"},
              {"_", "X", "_"},
              {"_", "_", "_"}
            });
    var gameId = UUID.randomUUID();
    var gameState = new GameState(gameId, gameBoard, List.of("X", "O"), 1);
    var gameStateAfterMove = gameState.afterPlayerMoves(3);
    var expectedGameState =
        new GameState(
            gameId,
            createBoardWith(
                new String[][] {
                  {"X", "O", "_"},
                  {"O", "X", "_"},
                  {"_", "_", "_"}
                }),
            List.of("X", "O"),
            0,
            3);
    Assert.assertEquals(gameStateAfterMove.lastMove(), 3);
    Assert.assertEquals(gameStateAfterMove.asJsonString(), expectedGameState.asJsonString());
  }
}
