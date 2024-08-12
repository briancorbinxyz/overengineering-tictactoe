package org.example;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;

import org.example.transport.tcp.TcpTransport;

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
            Socket socketPlayerOne = serverSocket.accept();
            Socket socketPlayerTwo = serverSocket.accept();
            executor.submit(
                    () -> {
                        try (var playerX = new RemotePlayer<BotPlayer>("X", new TcpTransport(socketPlayerOne));
                                var playerO = new RemotePlayer<BotPlayer>("O", new TcpTransport(socketPlayerTwo))) {
                            log.log(
                                    Level.INFO,
                                    "{0} concurrent games in progress.",
                                    updateStatsAndGetConcurrentGames());
                            Game game = new Game(3, false, playerX, playerO);
                            game.play();
                        } catch (Exception e) {
                            log.log(Level.ERROR, e.getMessage(), e);
                            throw new RuntimeException(e);
                        } finally {
                            concurrentGames.decrement();
                        }
                    });
        }
    }

    private long updateStatsAndGetConcurrentGames() {
        concurrentGames.increment();
        totalGames.accumulate(1);
        long currentConcurrentGames = concurrentGames.longValue();
        maxConcurrentGames.accumulate(currentConcurrentGames);
        return currentConcurrentGames;
    }
}
