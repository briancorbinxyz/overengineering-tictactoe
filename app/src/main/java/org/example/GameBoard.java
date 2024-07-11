package org.example;

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

    public boolean placeAnCheckWinner(int location) {

        return false;
    }

    private String contentElseBlank(String unit) {
        return unit == null ? "\uFF3F" : unit;
    }

    private String blankElseIndex(String unit, int index) {
        return unit == null ? String.valueOf(index) : "\uFF3F";
    }

}