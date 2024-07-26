package org.example;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import org.example.RemoteBotPlayer.Client;

public class GameClient {

    private static final Logger log = System.getLogger(GameClient.class.getName());

    private final int maxGames;

    private final String serverHost;

    private final int serverSocket;

    private final LongAdder submittedClients = new LongAdder();

    private final LongAdder completedClients = new LongAdder();

    private final LongAdder failedClients = new LongAdder();

    private final LongAdder startedClients = new LongAdder();

    public GameClient(int maxGames, String serverHost, int serverPort) {
        this.maxGames = maxGames;
        this.serverHost = serverHost;
        this.serverSocket = serverPort;
    }

    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        log.log(Level.INFO, "Client connecting for Tic-Tac-Toe.");
        long elapsed = System.currentTimeMillis();
        GameClient client =
                new GameClient(
                        1000,
                        args.length > 0 ? args[0] : "localhost",
                        args.length > 1 ? Integer.parseInt(args[1]) : 9090);
        try {
            client.connectToServer(executor);
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.MINUTES);
            elapsed = System.currentTimeMillis() - elapsed;
            log.log(Level.INFO, "Elapsed: " + elapsed);
        } finally {
            log.log(Level.INFO, "Finished.");
            log.log(
                    Level.INFO,
                    "Submitted "
                            + client.submittedClients.sum()
                            + " clients for "
                            + client.maxGames
                            + " games.");
            log.log(
                    Level.INFO,
                    "Started "
                            + client.startedClients.sum()
                            + " clients for "
                            + client.maxGames
                            + " games.");
            log.log(
                    Level.INFO,
                    "Failed "
                            + client.failedClients.sum()
                            + " clients for "
                            + client.maxGames
                            + " games.");
            log.log(
                    Level.INFO,
                    "Completed "
                            + client.failedClients.sum()
                            + " clients for "
                            + client.maxGames
                            + " games.");
        }
    }

    private void connectToServer(ExecutorService executor) {
        while (submittedClients.sum() < 2 * maxGames) {
            executor.submit(
                    () -> {
                        try (
                        // Contention will cause SocketException, down Server ConnectException
                        Socket socket = new Socket(serverHost, serverSocket);
                                Client client = new Client(socket); ) {
                            startedClients.increment();
                            log.log(Level.INFO, "Connected " + startedClients.sum());
                            client.connectAndPlay(socket);
                            completedClients.increment();
                        } catch (ConnectException e) {
                            failedClients.increment();
                            log.log(Level.INFO, "Connect exception, server down.");
                        } catch (SocketException e) {
                            failedClients.increment();
                            log.log(Level.INFO, "Socket exception, server disconnected.");
                        } catch (Exception e) {
                            failedClients.increment();
                            throw new RuntimeException(e);
                        }
                    });
            submittedClients.increment();
        }
    }
}
