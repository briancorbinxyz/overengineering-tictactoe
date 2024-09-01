package org.xxdc.oss.example;

import java.lang.System.Logger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

@Ignore
public class GamePerformanceTest {

  private static final Logger log = System.getLogger(GamePerformanceTest.class.getName());

  @Test
  public void testGameBotPerformanceInSerial() throws Exception {
    for (int i = 0; i < 1000; i++) {
      Game game = new Game(3, false, newBotPlayer("X"), newBotPlayer("O"));
      game.play();
      game.close();
    }
  }

  @Test
  public void testGameBotPerformanceInParallel() throws Exception {
    ExecutorService executor =
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    IntStream.range(0, 1000)
        .forEach(
            i ->
                executor.submit(
                    () -> {
                      try {
                        Game game = new Game(3, false, newBotPlayer("X"), newBotPlayer("O"));
                        game.play();
                        game.close();
                      } catch (Exception e) {
                        throw new RuntimeException(e);
                      }
                    }));
    executor.shutdown();
    executor.awaitTermination(10, java.util.concurrent.TimeUnit.MINUTES);
  }

  @Test
  public void testGameBotPerformanceInParallelWithVirtualThreads() throws Exception {
    ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    IntStream.range(0, 1000)
        .forEach(
            i ->
                executor.submit(
                    () -> {
                      try {
                        Game game = new Game(3, false, newBotPlayer("X"), newBotPlayer("O"));
                        game.play();
                        game.close();
                      } catch (Exception e) {
                        throw new RuntimeException(e);
                      }
                    }));
    executor.shutdown();
    executor.awaitTermination(10, java.util.concurrent.TimeUnit.MINUTES);
  }

  private PlayerNode newBotPlayer(String playerMarker) {
    return new PlayerNode.Local<>(playerMarker, new BotPlayer());
  }
}
