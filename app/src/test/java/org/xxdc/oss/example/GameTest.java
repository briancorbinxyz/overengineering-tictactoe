package org.xxdc.oss.example;

import org.testng.annotations.Test;

import org.xxdc.oss.example.BotPlayer;
import org.xxdc.oss.example.Game;
import org.xxdc.oss.example.PlayerNode;

public class GameTest {

  @Test
  public void test_game_with_two_local_bot_players() {
    Game game = new Game(3, false, newBotPlayer("X"), newBotPlayer("O"));
    try {
      game.play();
      game.close();
    } catch (Exception e) {
      throw new AssertionError();
    }
  }

  private PlayerNode newBotPlayer(String playerMarker) {
    return new PlayerNode.Local<>(playerMarker, new BotPlayer());
  }
}
