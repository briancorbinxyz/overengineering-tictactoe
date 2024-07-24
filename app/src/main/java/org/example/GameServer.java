package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;

public class GameServer {

    private static final int CONNECTION_TIMEOUT = 30000;

    private final LongAdder concurrentGames = new LongAdder();

    private final LongAccumulator maxConcurrentGames = new LongAccumulator(Long::max, 0);

    private final LongAccumulator totalGames = new LongAccumulator(Long::sum, 0);

    public static void main(String[] args) throws Exception {
        GameServer server = new GameServer();
        try (
            ServerSocket serverSocket = new ServerSocket(args.length > 0 ? Integer.parseInt(args[0]) : 9090, 10000);
            ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        ) {
            serverSocket.setSoTimeout(CONNECTION_TIMEOUT);
            System.out.println("Starting game server at " + serverSocket);
            server.listenForPlayers(executor, serverSocket);
            executor.shutdown();
            executor.awaitTermination(10, java.util.concurrent.TimeUnit.MINUTES);
        } catch (SocketTimeoutException e) {
            System.out.println("Timed out after " + CONNECTION_TIMEOUT + "ms");
        } catch (Exception e) {
            System.out.println(e);
            throw new RuntimeException(e);
        } finally {
            System.out.println("Server shutting down.");
            System.out.println("Total games played: " + server.totalGames.get());
            System.out.println("Maximum number of concurrent games: " + server.maxConcurrentGames.get());
        }
    }

    private void listenForPlayers(ExecutorService executor, ServerSocket serverSocket) throws IOException {
        while(true) {
            Socket socketPlayerOne = serverSocket.accept();
            Socket socketPlayerTwo = serverSocket.accept();
            executor.submit(() -> {
                try (
                    var playerX = new RemoteBotPlayer("X", socketPlayerOne);
                    var playerO = new RemoteBotPlayer("O", socketPlayerTwo)
                ) {
                    System.out.println(updateStatsAndGetConcurrentGames() + " concurrent games in progress.");
                    Game game = new Game(3, false, playerX, playerO);
                    game.play();
                } catch (Exception e) {
                    System.out.println(e);
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
