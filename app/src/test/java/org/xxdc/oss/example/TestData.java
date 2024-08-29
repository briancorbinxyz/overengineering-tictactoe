package org.xxdc.oss.example;

public class TestData {
  public static GameBoard createBoardWith(String[][] content) {
    GameBoard board = new GameBoardNativeImpl(content.length);
    for (int row = 0; row < content.length; row++) {
      for (int col = 0; col < content[row].length; col++) {
        if (content[row][col] != null && !content[row][col].equals("_")) {
          board = board.withMove(content[row][col], row * content.length + col);
        }
      }
    }
    return board;
  }
}
