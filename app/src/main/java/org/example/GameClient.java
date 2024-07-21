package org.example;

import java.net.ConnectException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import org.example.ClientServerBotPlayer.Client;

public class GameClient {

    private final int maxGames;

    private final String serverHost;

    private final int serverSocket;

    private final LongAdder submittedClients = new LongAdder();

    private final LongAdder startedClients = new LongAdder();

    public GameClient(int maxGames, String serverHost, int serverPort) {
        this.maxGames = maxGames;
        this.serverHost = serverHost;
        this.serverSocket = serverPort;
    }

    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        System.out.println("Client connecting for Tic-Tac-Toe.");
        long elapsed = System.currentTimeMillis();
        GameClient client = new GameClient(1000, "corbinm1mac.local", args.length > 0 ? Integer.parseInt(args[0]) : 9090);
        client.connectToServer(executor);
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.MINUTES);
        elapsed = System.currentTimeMillis() - elapsed;
        System.out.println("Elapsed: " + elapsed);
    }

    private void connectToServer(ExecutorService executor) {
        while (startedClients.sum() < 2 * maxGames) {
            submittedClients.increment();
            executor.submit(() -> {
                try (
                    // Contention will cause SocketException, down Server ConnectException
                    Socket socket = new Socket(serverHost, serverSocket);
                    Client client = new Client(socket);
                ) {
                    startedClients.increment();
                    System.out.println("Connected " + startedClients.sum());
                    client.connectAndPlay(socket);
                } catch (ConnectException e) {
                    System.out.println("Connect exception, server down.");
                    System.exit(-1);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
        System.out.println("Finished.");
        System.out.println("Submitted " + submittedClients.sum() + " clients for " + maxGames + " games.");
        System.out.println("Started " + startedClients.sum() + " clients for " + maxGames + " games.");
    }
}
