package org.xxdc.oss.example;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.stream.Collectors;
import java.util.stream.Gatherers;
import org.testng.annotations.Test;

public class GameplayTest {

  private static final Logger log = System.getLogger(GameplayTest.class.getName());

  @Test
  public void test_game_with_two_bot_players() {
    var gameOld = new Game(3, false, newBotPlayer("X"), newBotPlayer("O"));
    var game =
        new Game(
            3,
            false,
            new PlayerNode.Local<>("X", new BotPlayer()),
            new PlayerNode.Local<>("O", new BotPlayer()));
    try {
      game.play();
      game.close();
    } catch (Exception e) {
      log.log(Level.ERROR, e.getMessage(), e);
      throw new AssertionError();
    }
    var moves =
        game.history().stream()
            .skip(1) // Initial State
            .gather(Gatherers.windowSliding(game.numberOfPlayers()))
            .map(
                states ->
                    states.stream()
                        .map(
                            state ->
                                String.format(
                                    "%s->%s",
                                    state.playerMarkers().get(state.lastPlayerIndex()),
                                    state.lastMove()))
                        .collect(Collectors.joining(",", "[", "]")))
            .collect(Collectors.joining(" | "));
    log.log(Level.INFO, "Moves: {0}", moves);
  }

  private PlayerNode newBotPlayer(String playerMarker) {
    return new PlayerNode.Local<>(playerMarker, new BotPlayer());
  }
}
