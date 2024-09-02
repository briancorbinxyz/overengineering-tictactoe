package org.xxdc.oss.example;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class GameBoardTest {

  @Test
  public void test_default_game_board_type_is_native() {
    // should use the native implementation by default since it's in the classpath
    GameBoard gameBoard = GameBoard.withDimension(3);
    assertEquals(gameBoard.getClass(), GameBoardNativeImpl.class);
  }
}
