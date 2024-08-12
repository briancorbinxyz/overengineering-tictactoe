package org.example;

/// Tic-tac-toe player interface for all players
/// {@snippet :
/// // Create a human player
/// Player player = new HumanPlayer("X"); // @highlight region="player" substring="player"
///
/// // Choose the next valid move on the game board
/// int validBoardLocation = player.nextMove(gameBoard); // @end
/// }
public sealed interface Player permits HumanPlayer, BotPlayer, RemotePlayer {

    /// Returns the marker (e.g. "X" or "O") used by this player.
    /// @return the player's marker
    String playerMarker();

    /// Chooses the next valid move on the game board.
    /// @param board the current state of the game board
    /// @return the index of the next valid move on the board
    int nextMove(GameBoard board);
}
