package org.example;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Represents a game board for a game, such as tic-tac-toe. The board has a specified dimension, and
 * stores the current state of the game in a 1D array. Provides methods to check the validity of
 * moves, place player markers, check for a winner, and get a string representation of the board.
 */
public record GameBoard(int dimension, String[] content) implements Serializable {

    private static final long serialVersionUID = 1L;

    public GameBoard(int dimension) {
        this(dimension, new String[dimension * dimension]);
    }

    public GameBoard {
        if (content.length != dimension * dimension) {
            throw new IllegalArgumentException(
                    "Content must be of length " + dimension * dimension);
        }
    }

    public String toString() {
        return boardAsString();
    }

    public String validBoardPlacementsAsString() {
        String boardString = "";
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                int index = j + i * dimension;
                boardString +=
                        blankElseIndex(content[index], index) + (j + 1 < dimension ? " " : "");
            }
            boardString += "\n";
        }
        return boardString;
    }

    private String boardAsString() {
        String boardString = "";
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                int index = j + i * dimension;
                boardString += contentElseBlank(content[index]);
            }
            boardString += "\n";
        }
        return boardString;
    }

    public boolean isValidMove(int location) {
        return location >= 0 && location < content.length && content[location] == null;
    }

    public boolean hasChain(String playerMarker) {
        // check rows
        for (int i = 0; i < dimension; i++) {
            int chain = 0;
            for (int j = 0; j < dimension; j++) {
                if (playerMarker.equals(content[i * dimension + j])) {
                    chain++;
                }
                if (chain == dimension) {
                    return true;
                }
            }
        }
        // check columns
        for (int i = 0; i < dimension; i++) {
            int chain = 0;
            for (int j = 0; j < dimension; j++) {
                if (playerMarker.equals(content[j * dimension + i])) {
                    chain++;
                }
                if (chain == dimension) {
                    return true;
                }
            }
        }
        // check diagonals
        {
            int chain = 0;
            for (int i = 0; i < dimension; i++) {
                if (playerMarker.equals(content[i + (dimension * (i + 1)) - dimension])) {
                    chain++;
                }
                if (chain == dimension) {
                    return true;
                }
            }
        }
        {
            int chain = 0;
            for (int i = 0; i < dimension; i++) {
                if (playerMarker.equals(content[(dimension * (i + 1)) - (i + 1)])) {
                    chain++;
                }
                if (chain == dimension) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasMovesAvailable() {
        return Arrays.stream(content).anyMatch(m -> m == null);
    }

    private String contentElseBlank(String unit) {
        return unit == null ? "\u005F" : unit;
    }

    private String blankElseIndex(String unit, int index) {
        return unit == null ? String.valueOf(index) : "\u005F";
    }

    public GameBoard withMove(String playerMarker, int location) {
        if (!isValidMove(location)) {
            throw new InvalidMoveException();
        }
        String[] boardCopy = getBoardCopy();
        boardCopy[location] = playerMarker;
        return new GameBoard(dimension, boardCopy);
    }

    private String[] getBoardCopy() {
        String[] boardCopy = new String[dimension * dimension];
        System.arraycopy(content, 0, boardCopy, 0, boardCopy.length);
        return boardCopy;
    }

    public int getDimension() {
        return dimension;
    }
}
