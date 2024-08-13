package org.example;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import org.example.transport.tcp.TcpTransportServer;
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
                                                Game game =
                                                        new Game(
                                                                3,
                                                                false,
                                                                newBotPlayer("X"),
                                                                newBotPlayer("O"));
                                                game.play();
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
                                                Game game =
                                                        new Game(
                                                                3,
                                                                false,
                                                                newBotPlayer("X"),
                                                                newBotPlayer("O"));
                                                game.play();
                                            } catch (Exception e) {
                                                throw new RuntimeException(e);
                                            }
                                        }));
        executor.shutdown();
        executor.awaitTermination(10, java.util.concurrent.TimeUnit.MINUTES);
    }

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

    private PlayerNode newBotPlayer(String playerMarker) {
        return new PlayerNode.Local<>(new BotPlayer(playerMarker));
    }
}
