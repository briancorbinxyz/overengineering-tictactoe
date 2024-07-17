package org.example;

public sealed interface Player permits HumanPlayer, BotPlayer, LegacyPlayer {
    
    String getPlayerMarker();

    int nextMove(GameBoard board);

}