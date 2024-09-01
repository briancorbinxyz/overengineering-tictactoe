package org.xxdc.oss.example;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import org.xxdc.oss.example.bot.BotStrategy;
import org.xxdc.oss.example.transport.tcp.TcpTransports;

/**
 * The `GameClient` class is responsible for connecting to a game server and managing the execution
 * of game clients. It submits game clients to an executor service, tracks the status of the
 * clients, and logs relevant information.
 *
 * <p>The `main` method is the entry point of the application, which creates a `GameClient` instance
 * and connects it to the server. The `connectToServer` method is responsible for submitting game
 * clients to the executor service and handling any exceptions that may occur during the connection
 * process.
 */
public class GameClient {

  private static final Logger log = System.getLogger(GameClient.class.getName());

  private final int maxGames;

  private final String serverHost;

  private final int serverSocket;

  private final LongAdder submittedClients = new LongAdder();

  private final LongAdder completedClients = new LongAdder();

  private final LongAdder failedClients = new LongAdder();

  private final LongAdder startedClients = new LongAdder();

  /**
   * Constructs a new `GameClient` instance with the specified maximum number of games, server host,
   * and server port.
   *
   * @param maxGames The maximum number of games to be played.
   * @param serverHost The hostname or IP address of the game server.
   * @param serverPort The port number of the game server.
   */
  public GameClient(int maxGames, String serverHost, int serverPort) {
    this.maxGames = maxGames;
    this.serverHost = serverHost;
    this.serverSocket = serverPort;
  }

  /**
   * The `main` method is the entry point of the application, which creates a `GameClient` instance
   * and connects it to the server. It sets up an `ExecutorService` to manage the execution of game
   * clients, logs the start of the client connection, and then calls the `connectToServer` method
   * to submit the game clients. After the game clients have completed, it logs the final statistics
   * of the client execution, including the number of submitted, started, failed, and completed
   * clients.
   *
   * @param args Command-line arguments, where the first argument is the server host (default is
   *     "localhost") and the second argument is the server port (default is 9090).
   * @throws Exception If any unexpected exceptions occur during the execution of the game clients.
   */
  public static void main(String[] args) throws Exception {
    ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    log.log(Level.INFO, "Client connecting for Tic-Tac-Toe.");
    long elapsed = System.currentTimeMillis();
    GameClient client =
        new GameClient(
            1000,
            args.length > 0 ? args[0] : "localhost",
            args.length > 1 ? Integer.parseInt(args[1]) : 9090);
    try {
      client.connectToServer(executor);
      executor.shutdown();
      executor.awaitTermination(10, TimeUnit.MINUTES);
      elapsed = System.currentTimeMillis() - elapsed;
      log.log(Level.INFO, "Elapsed: " + elapsed);
    } finally {
      log.log(Level.INFO, "Finished.");
      log.log(
          Level.INFO,
          "Submitted {0} clients for {1} games.",
          client.submittedClients.sum(),
          client.maxGames);
      log.log(
          Level.INFO,
          "Started {0} clients for {1} games.",
          client.startedClients.sum(),
          client.maxGames);
      log.log(
          Level.INFO,
          "Failed {0} clients for {1} games.",
          client.failedClients.sum(),
          client.maxGames);
      log.log(
          Level.INFO,
          "Completed {0} clients for {1} games.",
          client.completedClients.sum(),
          client.maxGames);
    }
  }

  private void connectToServer(ExecutorService executor) {
    while (submittedClients.sum() < 2 * maxGames) {
      try {
        // Slow down the client to avoid saturating the server
        Thread.sleep(5L);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException(e);
      }
      executor.submit(
          () -> {
            try (
            // Contention will cause SocketException, down Server ConnectException
            var socket = new Socket(serverHost, serverSocket);
                var client =
                    TcpTransports.newTcpTransportClient(
                        new BotPlayer(BotStrategy.MINIMAX), socket); ) {
              startedClients.increment();
              socket.setKeepAlive(true);
              log.log(Level.INFO, "Started {0} clients.", startedClients.sum());
              client.run();
              completedClients.increment();
            } catch (ConnectException _) {
              failedClients.increment();
              log.log(Level.ERROR, "Connect exception, server down.");
            } catch (SocketException e) {
              failedClients.increment();
              log.log(Level.ERROR, "Socket exception, server disconnected: {}", e.getMessage());
            } catch (Exception e) {
              failedClients.increment();
              log.log(Level.ERROR, "Unexpected exception: {}", e.getMessage());
              throw new RuntimeException(e);
            }
          });
      submittedClients.increment();
    }
  }
}
