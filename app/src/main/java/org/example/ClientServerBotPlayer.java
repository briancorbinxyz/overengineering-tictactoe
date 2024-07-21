package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.random.RandomGenerator;

/**
 * Represents a bot player in the game. The bot player uses a random number generator to make moves on the game board.
 */
public final class ClientServerBotPlayer implements Player, Serializable, AutoCloseable {

    private static final long serialVersionUID = 1L;

    private final String playerMarker;

    private transient BufferedReader in;

    private transient PrintWriter out;

    public String getPlayerMarker() {
        return playerMarker;
    };

    public ClientServerBotPlayer(String playerMarker, Socket socket) throws Exception {
        this.playerMarker = playerMarker;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
        System.out.println("Server connecting client to socket " + socket + " for Tic-Tac-Toe.");
    }

    public int nextMove(GameBoard board) {
        int location = -1;
        do { 
            try {
                // No protocol yet, just send the board and get the next move
                out.println("9x9");
                String clientMessage = in.readLine();
                location = Integer.parseInt(clientMessage);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } 
        } while (!board.isValidMove(location));
        return location;
    }

    @Override
    public void close() throws Exception {
        out.close();
        in.close();
    };

    public static record Client(BufferedReader in, PrintWriter out, RandomGenerator randomGenerator) implements Serializable, AutoCloseable {

        public Client(Socket socket) throws Exception {
            this(new BufferedReader(new InputStreamReader(socket.getInputStream())), new PrintWriter(socket.getOutputStream(), true), new SecureRandom());
        }
        
        public void connectAndPlay(Socket socket) {
            try {
                // For now read the board and send a random move
                String serverMessage;
                while ((serverMessage = in.readLine()) != null) {
                    // System.out.println("DEBUG: " + serverMessage);
                    int nextMove = randomGenerator.nextInt(9);
                    // System.out.println(this + " sending move: " + nextMove);
                    out.println(nextMove);
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }


        @Override
        public void close() throws Exception {
            out.close();
            in.close();
        };

    }
    
}

