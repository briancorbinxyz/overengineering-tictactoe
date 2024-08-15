package org.example;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Supplier;

import org.example.transport.tcp.TcpTransportServer;

public class GameServer {

    private static final Logger log = System.getLogger(GameServer.class.getName());

    private static final int CONNECTION_TIMEOUT = 30000;

    private final LongAdder concurrentGames = new LongAdder();

    private final LongAccumulator maxConcurrentGames = new LongAccumulator(Long::max, 0);

    private final LongAccumulator totalGames = new LongAccumulator(Long::sum, 0);

    public static void main(String[] args) throws Exception {
        GameServer server = new GameServer();
        try (ServerSocket serverSocket =
                        new ServerSocket(
                                args.length > 0 ? Integer.parseInt(args[0]) : 9090, 10000);
                ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor(); ) {
            serverSocket.setSoTimeout(CONNECTION_TIMEOUT);
            log.log(Level.INFO, "Starting game server at {0}", serverSocket);
            server.listenForPlayers(executor, serverSocket);
            executor.shutdown();
            executor.awaitTermination(10, java.util.concurrent.TimeUnit.MINUTES);
        } catch (SocketTimeoutException e) {
            log.log(Level.INFO, "Timed out after {0}ms", CONNECTION_TIMEOUT);
        } catch (Exception e) {
            log.log(Level.INFO, e);
            throw new RuntimeException(e);
        } finally {
            log.log(Level.INFO, "Server shutting down.");
            log.log(Level.INFO, "Total games played: {0}", server.totalGames.get());
            log.log(
                    Level.INFO,
                    "Maximum number of concurrent games: {0}",
                    server.maxConcurrentGames.get());
        }
    }

    private void listenForPlayers(ExecutorService executor, ServerSocket serverSocket)
            throws IOException {
        while (true) {
            var clientSocket1Future = CompletableFuture.supplyAsync(clientSocketAsync(serverSocket), executor);
            var clientSocket2Future = CompletableFuture.supplyAsync(clientSocketAsync(serverSocket), executor);
            try {
                clientSocket1Future.thenCombineAsync(clientSocket2Future, (clientSocket1, clientSocket2) -> {
                    try {
                        var playerX = new PlayerNode.Remote( "X", new TcpTransportServer(clientSocket1));
                        var playerO = new PlayerNode.Remote( "O", new TcpTransportServer(clientSocket2));
                        log.log( Level.INFO, "{0} concurrent games in progress.", updateStatsAndGetConcurrentGames());
                        Game game = new Game(3, false, playerX, playerO);
                        game.play();
                        game.close();
                        return null;
                    } catch (Exception e) {
                        log.log(Level.ERROR, e.getMessage(), e);
                        throw new RuntimeException(e);
                    } finally {
                        concurrentGames.decrement();
                    }
                }, executor).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new GameServiceException("Error during game execution: " + e.getMessage(), e);
            }
        }
    }

    private Supplier<Socket> clientSocketAsync(ServerSocket serverSocket) {
        return () -> {
            try {
                return serverSocket.accept();
            } catch (IOException e) {
                log.log(Level.INFO, e);
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
