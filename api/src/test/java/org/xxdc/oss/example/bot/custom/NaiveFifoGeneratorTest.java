package org.xxdc.oss.example.bot.custom;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class NaiveFifoGeneratorTest {

  private CustomBotStrategy botStrategy;

  @BeforeClass
  public void setupAll() {
    // Share Definition to avoid duplicate:
    // java.lang.LinkageError: loader 'app' attempted duplicate class definition for
    // org.xxdc.oss.example.bot.custom.NaiveFifo
    botStrategy = NaiveFifoGenerator.newGeneratedBot();
  }

  @Test
  public void test_should_generate_naive_bot() {
    assertNotNull(botStrategy);
  }

  @Test
  public void test_should_generate_naive_bot_choosing_first_available_move() {
    var availableMoves = List.of(42, 10, 0, 1);
    assertEquals(botStrategy.bestMove(availableMoves), 42);
  }

  @Test
  public void test_should_generate_naive_bot_choosing_first_available_move_v2() {
    var availableMoves = List.of(10, 0, 1);
    assertEquals(botStrategy.bestMove(availableMoves), 10);
  }

  @Test(expectedExceptions = RuntimeException.class)
  public void test_should_generate_naive_bot_throwing_exception_when_no_moves_are_available() {
    var availableMoves = new ArrayList<Integer>();
    botStrategy.bestMove(availableMoves);
  }
}
