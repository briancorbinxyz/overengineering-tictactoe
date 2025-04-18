package org.xxdc.oss.example;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Supplier;
import org.xxdc.oss.example.transport.tcp.TcpTransportServer;

/**
 * The `GameServer` class is responsible for managing the game server that hosts the tic-tac-toe
 * game. It listens for incoming connections, creates game sessions, and tracks various game
 * statistics.
 */
public class GameServer {

  private static final Logger log = System.getLogger(GameServer.class.getName());

  private static final int CONNECTION_TIMEOUT = 30000;

  private final LongAdder concurrentGames = new LongAdder();

  private final LongAccumulator maxConcurrentGames = new LongAccumulator(Long::max, 0);

  private final LongAccumulator totalGames = new LongAccumulator(Long::sum, 0);

  /**
   * The `main` method is the entry point for the GameServer application. It creates a new
   * `GameServer` instance, sets up a `ServerSocket` to listen for incoming connections, and starts
   * the `listenForPlayers` method to handle incoming players and start new games.
   *
   * <p>The method first creates a `ServerSocket` on the specified port (or 9090 if no port is
   * provided) with a backlog of 10,000 connections. It then creates a new `ExecutorService` using
   * the `newVirtualThreadExecutor` method to handle the asynchronous game sessions.
   *
   * <p>The `listenForPlayers` method is called to start the server and listen for incoming player
   * connections. If any exceptions occur during the server's operation, the `handleException`
   * method is called to log the error.
   *
   * <p>Finally, the method logs the total number of games played and the maximum number of
   * concurrent games before the server shuts down.
   *
   * @param args the command-line arguments passed to the application
   * @throws Exception if there is an error starting the server
   */
  public static void main(String[] args) throws Exception {
    GameServer server = new GameServer();
    try (ServerSocket serverSocket =
            new ServerSocket(args.length > 0 ? Integer.parseInt(args[0]) : 9090, 10000);
        ExecutorService executor = newVirtualThreadExecutor(); ) {
      serverSocket.setSoTimeout(CONNECTION_TIMEOUT);
      log.log(Level.INFO, "Starting tic-tac-toe game server at {0}", serverSocket);
      server.listenForPlayers(executor, serverSocket);
    } catch (Exception e) {
      handleException(e);
    } finally {
      log.log(Level.INFO, "Server shutting down...");
      log.log(Level.INFO, "Total games played: {0}", server.totalGames.get());
      log.log(
          Level.INFO, "Maximum number of concurrent games: {0}", server.maxConcurrentGames.get());
    }
  }

  private static ExecutorService newVirtualThreadExecutor() {
    var threadFactory = Thread.ofVirtual().name("ttt-virtual-", 1).factory();
    ExecutorService executor = Executors.newThreadPerTaskExecutor(threadFactory);
    return executor;
  }

  private static void handleException(Exception e) {
    Throwable cause = e;
    while (cause.getCause() != null) {
      cause = cause.getCause();
    }
    switch (cause) {
      case SocketTimeoutException se ->
          log.log(Level.INFO, "Server connection timed out after {0}ms.", CONNECTION_TIMEOUT);
      default -> log.log(Level.ERROR, "Unexpected exception: {0}", e.getMessage(), e);
    }
  }

  private void listenForPlayers(ExecutorService executor, ServerSocket serverSocket)
      throws IOException {
    while (true) {
      log.log(Level.INFO, "Waiting for players to connect...");
      var clientSocket1Future = CompletableFuture.supplyAsync(clientSocket(serverSocket), executor);
      // block
      var clientSocket2Future = CompletableFuture.completedFuture(clientSocket(serverSocket).get());
      // run game asynchronously
      clientSocket1Future.thenCombineAsync(
          clientSocket2Future,
          (clientSocket1, clientSocket2) -> {
            try {
              var playerX = new PlayerNode.Remote("X", new TcpTransportServer(clientSocket1));
              var playerO = new PlayerNode.Remote("O", new TcpTransportServer(clientSocket2));
              log.log(
                  Level.INFO,
                  "{0} concurrent games in progress.",
                  updateStatsAndGetConcurrentGames());
              Game game = new Game(3, false, playerX, playerO);
              game.play();
              game.close();
              return game;
            } catch (Exception e) {
              throw new RuntimeException(e);
            } finally {
              concurrentGames.decrement();
            }
          },
          executor);
    }
  }

  private Supplier<Socket> clientSocket(ServerSocket serverSocket) {
    return () -> {
      try {
        return serverSocket.accept();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    };
  }

  private long updateStatsAndGetConcurrentGames() {
    concurrentGames.increment();
    totalGames.accumulate(1);
    long currentConcurrentGames = concurrentGames.longValue();
    maxConcurrentGames.accumulate(currentConcurrentGames);
    return currentConcurrentGames;
  }
}
