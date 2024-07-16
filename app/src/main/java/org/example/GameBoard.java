package org.example;

import java.util.Arrays;

public class GameBoard {
    private final int dimension;
    private final String [] content;

    public GameBoard(int dimension) {
        this.dimension = dimension;
        this.content = new String[dimension * dimension];
    }

    public String toString() {
        return boardAsString();
    }

    public String validBoardPlacementsAsString() {
        String boardString = "";
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                int index = j + i*dimension; 
                boardString += blankElseIndex(content[index], index) + (j + 1 < dimension ? " " : "");
            }
            boardString += "\n";
        }
        return boardString;
    }

    private String boardAsString() {
        String boardString = "";
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                int index = j + i*dimension; 
                boardString += contentElseBlank(content[index]);
            }
            boardString += "\n";
        }
        return boardString;
    }

    public boolean isValidMove(int location) {
        return location >= 0 && location < content.length && content[location] == null;
    }

    public boolean checkWinner(String playerMarker) {
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
                if (playerMarker.equals(content[i+(dimension*(i+1))-dimension])) {
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
                if (playerMarker.equals(content[(dimension*(i+1))-(i+1)])) {
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

    public void placePlayerMarker(String playerMarker, int location) {
        if (!isValidMove(location)) {
            throw new InvalidMoveException();
        }
        this.content[location] = playerMarker;
    }

    public int getDimension() {
        return dimension;
    }

}