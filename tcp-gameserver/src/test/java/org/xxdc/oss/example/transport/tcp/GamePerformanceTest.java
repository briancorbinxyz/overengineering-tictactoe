package org.xxdc.oss.example.transport.tcp;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import org.xxdc.oss.example.Game;
import org.xxdc.oss.example.PlayerNode;

@Ignore
public class GamePerformanceTest {

  private static final Logger log = System.getLogger(GamePerformanceTest.class.getName());

  @Test
  public void testGameClientServerBotPerformanceInParallelWithPlatformThreads() throws Exception {
    ExecutorService executor =
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    try (ServerSocket serverSocket = new ServerSocket(9090)) {
      // round-robin match making for up to 1000 games
      execClientServerGames(executor, serverSocket);
    } catch (Exception e) {
      System.out.println(e);
      throw new RuntimeException(e);
    }
    executor.shutdown();
    executor.awaitTermination(10, java.util.concurrent.TimeUnit.MINUTES);
  }

  @Test
  public void testGameClientServerBotPerformanceInParallelWithVirtualThreads() throws Exception {
    ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    try (ServerSocket serverSocket = new ServerSocket(9090)) {
      // round-robin match making for up to 1000 games
      execClientServerGames(executor, serverSocket);
    } catch (Exception e) {
      System.out.println(e);
      throw new RuntimeException(e);
    }
    executor.shutdown();
    executor.awaitTermination(10, java.util.concurrent.TimeUnit.MINUTES);
  }

  private void execClientServerGames(ExecutorService executor, ServerSocket serverSocket)
      throws IOException {
    for (int i = 0; i < 40000; i++) {
      log.log(Level.INFO, "Accepting connections for game {0}", i);
      Socket playerOne = serverSocket.accept();
      Socket playerTwo = serverSocket.accept();
      executor.submit(
          () -> {
            try (var playerX = new PlayerNode.Remote("X", new TcpTransportServer(playerOne));
                var playerO = new PlayerNode.Remote("O", new TcpTransportServer(playerTwo))) {
              Game game = new Game(3, false, playerX, playerO);
              game.play();
              game.close();
            } catch (Exception e) {
              System.out.println(e);
              throw new RuntimeException(e);
            }
          });
    }
  }
}
