package org.xxdc.oss.example;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.xxdc.oss.example.BotPlayer;
import org.xxdc.oss.example.Game;
import org.xxdc.oss.example.HumanPlayer;
import org.xxdc.oss.example.Player;
import org.xxdc.oss.example.PlayerNode;
import org.xxdc.oss.example.transport.Transports;
import org.xxdc.oss.example.transport.tcp.TcpTransportServer;

import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

public class TcpTransportTest {

  private static final Logger log = System.getLogger(TcpTransportTest.class.getName());

  private static final String SERVER_HOST = "localhost";

  private static final int SERVER_PORT = getAvailablePort();

  @Test
  public void testCanCreateClientServerBotGame() throws Exception {
    createClientServerGame(BotPlayer::new, BotPlayer::new);
  }

  @Ignore("This is a manual test that needs to be run manually")
  public void testCanCreateClientServerHumanBotGame() throws Exception {
    createClientServerGame(HumanPlayer::new, BotPlayer::new);
  }

  private void createClientServerGame(Supplier<Player> p1Supplier, Supplier<Player> p2Supplier) {
    var executor = Executors.newVirtualThreadPerTaskExecutor();
    var serverSocketFuture = startServerAsync(SERVER_PORT, executor);
    try {
      // Wait for the server to start before starting clients
      serverSocketFuture
          .thenRun(() -> startClientAsync(SERVER_HOST, SERVER_PORT, p1Supplier::get, executor))
          .exceptionally(
              (e) -> {
                log.log(Level.ERROR, "P1 Exception: " + e.getMessage(), e);
                return null;
              });
      serverSocketFuture
          .thenRun(() -> startClientAsync(SERVER_HOST, SERVER_PORT, p2Supplier::get, executor))
          .exceptionally(
              (e) -> {
                log.log(Level.ERROR, "P2 Exception: " + e.getMessage(), e);
                return null;
              });

      // Wait for both clients to connect
      var client1SocketFuture = serverSocketFuture.thenCompose(s -> acceptClientAsync(s, executor));
      var client2SocketFuture = serverSocketFuture.thenCompose(s -> acceptClientAsync(s, executor));
      var connectedGame =
          client1SocketFuture.thenCombine(
              client2SocketFuture,
              (client1Socket, client2Socket) -> createGame(client1Socket, client2Socket));

      // Wait for both clients to finish connecting and start the game
      var game = connectedGame.get();
      game.play();
      log.log(Level.INFO, "[Server] Game complete.");

      // Close the server socket when done
      serverSocketFuture.thenAccept(TcpTransportTest::closeServerSocket);
      executor.shutdown();
      executor.awaitTermination(10, TimeUnit.MINUTES);
    } catch (InterruptedException | ExecutionException e) {
      log.log(Level.ERROR, e);
    }
  }

  private Game createGame(Socket client1Socket, Socket client2Socket) {
    log.log(Level.INFO, "[Server] Clients connected. Game ready to start.");
    return new Game(
        3,
        false,
        new PlayerNode.Remote("X", new TcpTransportServer(client1Socket)),
        new PlayerNode.Remote("O", new TcpTransportServer(client2Socket)));
  }

  private static CompletableFuture<ServerSocket> startServerAsync(
      int port, ExecutorService executor) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            var serverSocket = new ServerSocket(port);
            log.log(
                Level.INFO, "[Server] Game Server started. Listening on port {0,number,#}.", port);
            return serverSocket;
          } catch (IOException e) {
            throw new RuntimeException("Error starting game server on port " + port, e);
          }
        },
        executor);
  }

  private static void startClientAsync(
      String host, int port, Supplier<Player> player, ExecutorService executor) {
    CompletableFuture.runAsync(
        () -> {
          try {
            var socket = new Socket(host, port);
            log.log(Level.INFO, "[Client] Client connected to server on port {0,number,#}", port);
            var client = Transports.newTcpTransportClient(player.get(), socket);
            client.run();
          } catch (IOException e) {
            throw new RuntimeException("Error connecting to server on port " + port, e);
          }
        },
        executor);
  }

  private static CompletableFuture<Socket> acceptClientAsync(
      ServerSocket serverSocket, ExecutorService executor) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            Socket clientSocket = serverSocket.accept();
            log.log(Level.INFO, "[Server] Client connected to port " + serverSocket.getLocalPort());
            return clientSocket;
          } catch (IOException e) {
            throw new RuntimeException(
                "Error accepting client on port " + serverSocket.getLocalPort(), e);
          }
        },
        executor);
  }

  private static void closeServerSocket(ServerSocket serverSocket) {
    try {
      if (serverSocket != null && !serverSocket.isClosed()) {
        serverSocket.close();
        log.log(
            Level.INFO,
            "[Server] Server socket on port " + serverSocket.getLocalPort() + " closed");
      }
    } catch (IOException e) {
      throw new RuntimeException("Error connecting to server on port " + e.getMessage(), e);
    }
  }

  static int getAvailablePort() {
    try (ServerSocket socket = new ServerSocket(0)) {
      return socket.getLocalPort();
    } catch (IOException e) {
      throw new RuntimeException("Unable to find available port", e);
    }
  }
}
