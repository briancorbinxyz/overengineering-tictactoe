package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameServer {

    public static void main(String[] args) throws Exception {
        GameServer server = new GameServer();
        try (
            ServerSocket serverSocket = new ServerSocket(args.length > 0 ? Integer.parseInt(args[0]) : 9090);
            ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        ) {
            System.out.println("Starting game server at " + serverSocket);
            server.listenForPlayers(executor, serverSocket);
            executor.shutdown();
            executor.awaitTermination(10, java.util.concurrent.TimeUnit.MINUTES);
        } catch (Exception e) {
            System.out.println(e);
            throw new RuntimeException(e);
        }
    }

    private void listenForPlayers(ExecutorService executor, ServerSocket serverSocket) throws IOException {
        while(true) {
            Socket socketPlayerOne = serverSocket.accept();
            Socket socketPlayerTwo = serverSocket.accept();
            executor.submit(() -> {
                try (
                    var playerX = new ClientServerBotPlayer("X", socketPlayerOne);
                    var playerO = new ClientServerBotPlayer("O", socketPlayerTwo)
                ) {
                    Game game = new Game(3, playerX, playerO);
                    game.play();
                } catch (Exception e) {
                    System.out.println(e);
                    throw new RuntimeException(e);
                }
            });
        }
    }

}
