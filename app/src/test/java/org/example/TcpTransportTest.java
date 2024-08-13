package org.example;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.example.transport.Transports;
import org.example.transport.tcp.TcpTransportServer;
import org.testng.annotations.Test;

public class TcpTransportTest {

    private static final Logger log = System.getLogger(TcpTransportTest.class.getName());

    @Test
    public void testCanCreateClientServerBotGame() throws Exception {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        try (ServerSocket serverSocket = new ServerSocket(9090)) {
            // round-robin match making for up to 1000 games
            startServerGames(executor, serverSocket);
            startClientConnections(executor);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
        executor.shutdown();
        executor.awaitTermination(10, java.util.concurrent.TimeUnit.MINUTES);
    }

    private void startServerGames(ExecutorService executor, ServerSocket serverSocket)
            throws IOException {
        for (int i = 0; i < 1; i++) {
            log.log(Level.INFO, "Accepting connections for game {0}", i);
            Socket playerOne = serverSocket.accept();
            Socket playerTwo = serverSocket.accept();
            executor.submit(
                    () -> {
                        try (var playerX =
                                        new PlayerNode.Remote(
                                                "X", new TcpTransportServer(playerOne));
                                var playerO =
                                        new PlayerNode.Remote(
                                                "O", new TcpTransportServer(playerTwo))) {
                            Game game = new Game(3, false, playerX, playerO);
                            game.play();
                        } catch (Exception e) {
                            System.out.println(e);
                            throw new RuntimeException(e);
                        }
                    });
        }
    }

    private void startClientConnections(ExecutorService executor) {
        for (int i = 0; i < 2; i++) {
            executor.submit(
                    () -> {
                        try (
                        // Contention will cause SocketException, down Server ConnectException
                        var socket = new Socket("localhost", 9090);
                                var client =
                                        Transports.newTcpTransportClient(
                                                BotPlayer.class, socket); ) {
                        } catch (Exception e) {
                            throw new AssertionError(e);
                        }
                    });
        }
    }

    private PlayerNode newBotPlayer(String playerMarker) {
        return new PlayerNode.Local<>(new BotPlayer(playerMarker));
    }
}
