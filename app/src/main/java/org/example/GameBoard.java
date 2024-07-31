package org.example;

/**
 * Represents a game board for a game.
 * The game board has a square dimension and contains a grid of game pieces.
 * This interface defines the operations that can be performed on the game board.
 */
public interface GameBoard {

    /**
     * Returns a string representation of the game board.
     * @return a string representation of the game board.
     */
    String toString();

    /**
     * Checks if the given location on the game board is a valid move (i.e. an available location).
     * @param location the location on the game board to check
     * @return true if the location is a valid move, false otherwise
     */
    boolean isValidMove(int location);

    /**
     * Checks if the given player has a winning chain of connected game pieces on the game board.
     * @param playerMarker the marker representing the player to check for a chain
     * @return true if the player has a chain of connected game pieces, false otherwise
     */
    boolean hasChain(String playerMarker);

    /**
     * Checks if there are any available moves on the game board.
     * @return true if there are any available moves, false otherwise
     */
    boolean hasMovesAvailable();

    /**
     * Creates a new {@link GameBoard} instance with the given player's move made at the specified location.
     * @param playerMarker the marker representing the player making the move
     * @param location the location on the game board where the player is making the move
     * @return a new {@link GameBoard} instance with the player's move applied
     */
    GameBoard withMove(String playerMarker, int location);

    /**
     * Returns the dimension of the game board, which is the number of rows or columns.
     * @return the dimension of the game board
     */
    int getDimension();

    /**
     * Returns the contents of the game board as an array of strings representing the game pieces.
     * The array is of dimension {@link #getDimension()} x {@link #getDimension()}, where the value at
     * the i,j index is the game piece at the i,j location on the game board.
     * @return the contents of the game board as an array of strings
     */
    String[] getContent();
}
