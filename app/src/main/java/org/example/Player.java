package org.example;

public sealed interface Player permits HumanPlayer, BotPlayer {
    
    String getPlayerMarker();

    int nextMove(GameBoard board);

}