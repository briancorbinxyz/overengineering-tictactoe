package org.example;

/**
 * Tic-tac-toe player interface for all players 
 * {@snippet :
 * // Create a human player
 * Player player = new HumanPlayer("X"); // @highlight region="player" substring="player"
 * 
 * // Choose the next valid move on the game board
 * int validBoardLocation = player.nextMove(gameBoard); // @end
 * }
 */
public sealed interface Player permits HumanPlayer, BotPlayer, ClientServerBotPlayer {
    
    String getPlayerMarker();

    int nextMove(GameBoard board);

}