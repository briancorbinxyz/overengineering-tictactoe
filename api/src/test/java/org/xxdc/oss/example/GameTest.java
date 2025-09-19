package org.xxdc.oss.example;

import static org.testng.Assert.assertFalse;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

public class GameTest {

  @Test
  public void test_game_with_two_local_bot_players() {
    Game game = new Game(3, false, newBotPlayer("X"), newBotPlayer("O"));
    try {
      game.play();
      game.close();
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  @Test
  public void test_game_context_is_only_set_during_play() {
    assertFalse(Game.isGameContextSet());
    try (Game game = new Game(3, false, newBotPlayer("X"), newBotPlayer("O"))) {
      game.playWithAction(g -> assertTrue(Game.isGameContextSet()));
    } catch (Exception e) {
      throw new AssertionError(e);
    }
    assertFalse(Game.isGameContextSet());
  }

  @Test
  public void test_game_context_is_set_during_play_correctly() {
    try (Game game = new Game(3, false, newBotPlayer("X"), newBotPlayer("O"))) {
      game.playWithAction(
          g -> {
            var maybeGameContext = Game.gameContext();
            assertTrue(maybeGameContext.isPresent());
            var gameContext = maybeGameContext.get();
            assertEquals(gameContext.id(), game.id().toString());
          });
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  private PlayerNode newBotPlayer(String playerMarker) {
    return new PlayerNode.Local<>(playerMarker, new BotPlayer());
  }
}
