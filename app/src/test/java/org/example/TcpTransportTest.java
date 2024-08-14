package org.example;

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
import org.example.transport.Transports;
import org.example.transport.tcp.TcpTransportServer;

import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

@Ignore
public class TcpTransportTest {

    private static final Logger log = System.getLogger(TcpTransportTest.class.getName());

    private static final String SERVER_HOST = "localhost";

    private static final int SERVER_PORT = 9090;

    @Test
    public void testCanCreateClientServerBotGame() throws Exception {
        var executor = Executors.newVirtualThreadPerTaskExecutor();
        CompletableFuture<ServerSocket> serverSocketFuture = startServer(SERVER_PORT, executor);

        serverSocketFuture.thenAccept(
                serverSocket -> startClient(SERVER_HOST, SERVER_PORT, executor));
        serverSocketFuture.thenAccept(
                serverSocket -> startClient(SERVER_HOST, SERVER_PORT, executor));

        try {
            // Wait for both clients to connect
            CompletableFuture<Socket> client1ConnectionFuture =
                    serverSocketFuture.thenCompose(s -> acceptClient(s, executor));
            CompletableFuture<Socket> client2ConnectionFuture =
                    serverSocketFuture.thenCompose(s -> acceptClient(s, executor));
            CompletableFuture<Game> clientsConnectedFuture =
                    client1ConnectionFuture.thenCombine(
                            client2ConnectionFuture,
                            (client1Connection, client2Connection) -> {
                                log.log(Level.INFO, "[Server] Clients connected");
                                return new Game(
                                        3,
                                        false,
                                        new PlayerNode.Remote(
                                                "X", new TcpTransportServer(client1Connection)),
                                        new PlayerNode.Remote(
                                                "O", new TcpTransportServer(client2Connection)));
                            });

            // Wait for both clients to finish connecting and start the game
            var game = clientsConnectedFuture.get();
            game.play();
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        // Close the server socket when done
        serverSocketFuture.thenAccept(TcpTransportTest::closeServerSocket);
    }

    private static CompletableFuture<ServerSocket> startServer(int port, ExecutorService executor) {
        return CompletableFuture.supplyAsync(
                () -> {
                    try {
                        ServerSocket serverSocket = new ServerSocket(port);
                        log.log(
                                Level.INFO,
                                "[Server] Game Server started. Listening on port {0}.",
                                port);
                        return serverSocket;
                    } catch (IOException e) {
                        throw new RuntimeException("Error starting game server on port " + port, e);
                    }
                },
                executor);
    }

    private static void startClient(String host, int port, ExecutorService executor) {
        CompletableFuture.runAsync(
                () -> {
                    try {
                        Socket socket = new Socket(host, port);
                        log.log(
                                Level.INFO,
                                "[Client] Client connected to server on port {0}",
                                port);
                        var client = Transports.newTcpTransportClient(BotPlayer.class, socket);
                        client.run();
                    } catch (IOException e) {
                        throw new RuntimeException("Error connecting to server on port " + port, e);
                    }
                },
                executor);
    }

    private static CompletableFuture<Socket> acceptClient(
            ServerSocket serverSocket, ExecutorService executor) {
        return CompletableFuture.supplyAsync(
                () -> {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        log.log(
                                Level.INFO,
                                "[Server] Client connected to port " + serverSocket.getLocalPort());
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
                        "[Server] Server socket on port "
                                + serverSocket.getLocalPort()
                                + " closed");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
