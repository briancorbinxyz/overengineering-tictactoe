package org.example;

import org.testng.annotations.Test;

public class GameTest {

    @Test
    public void test_game_with_two_bot_players() {
        Game game = new Game(3, false, newBotPlayer("X"), newBotPlayer("O"));
        try {
            game.play();
        } catch (Exception e) {
            throw new AssertionError();
        }
    }

    private PlayerNode newBotPlayer(String playerMarker) {
        return new PlayerNode.Local<>(new BotPlayer(playerMarker));
    }
}
