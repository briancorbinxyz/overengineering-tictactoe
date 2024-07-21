package org.example;

import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.example.ClientServerBotPlayer.Client;

public class GameClient {

    private final int maxGames;

    private final int serverSocket;

    public GameClient(int maxGames, int serverSocket) {
        this.maxGames = maxGames;
        this.serverSocket = serverSocket;
    }

    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        System.out.println("Client connecting for Tic-Tac-Toe.");
        long elapsed = System.currentTimeMillis();
        GameClient client = new GameClient(1000, args.length > 0 ? Integer.parseInt(args[0]) : 9090);
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
                    Socket socket = new Socket("localhost", serverSocket);
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
