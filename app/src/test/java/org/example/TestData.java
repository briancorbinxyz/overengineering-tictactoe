package org.example;

public class TestData {
    public static GameBoard createBoardWith(String[][] content) {
        GameBoard board = new GameBoardNativeImpl(3);
        for (int row = 0; row < content.length; row++) {
            for (int col = 0; col < content[row].length; col++) {
                if (content[row][col] != null && !content[row][col].equals("_")) {
                    board = board.withMove(content[row][col], row * 3 + col);
                }
            }
        }
        return board;
    }
}
