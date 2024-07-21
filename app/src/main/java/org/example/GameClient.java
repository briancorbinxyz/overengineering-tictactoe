package org.example;

import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.example.ClientServerBotPlayer.Client;

public class GameClient {

    private final int maxGames;

    public GameClient(int maxGames) {
        this.maxGames = maxGames;
    }

    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        System.out.println("Client connecting for Tic-Tac-Toe.");
        long elapsed = System.currentTimeMillis();
        GameClient client = new GameClient(1000);
        client.connectToServer(executor);
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.MINUTES);
        elapsed = System.currentTimeMillis() - elapsed;
        System.out.println("Elapsed: " + elapsed);
    }

    private void connectToServer(ExecutorService executor) {
        for (int i = 0; i < 2 * maxGames; i++) {
            executor.submit(() -> {
                try (
                    Socket socket = new Socket("localhost", 9090);
                    Client client = new Client(socket);
                ) {
                    System.out.println("Connected " + client);
                    client.connectAndPlay(socket);
                } catch (Exception e) {
                    System.out.println(e);
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
