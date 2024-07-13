package org.example;

import java.util.List;
import java.util.Random;

public class Game {

    private final GameBoard board;

    private final List<String> playerMarkers;

    private int dimension;

    public Game() {
        this(3);
    }

    public Game(int dimension) {
        this.dimension = dimension;
        this.board = new GameBoard(dimension);
        this.playerMarkers = List.of("X", "O");
    }

    public void play() throws InterruptedException {
        boolean hasWinner = false;
        boolean movesAvailable = board.hasMovesAvailable();
        int playerIndex = 0;
        System.out.println(board);
        System.out.println();
        while (!hasWinner && movesAvailable) {
            Thread.sleep(10);
            String playerMarker = playerMarkers.get(playerIndex);
            Random r = new Random();
            int location;
            do { 
                location = r.nextInt(dimension*dimension);
            } while (!board.isValidMove(location));
            board.placePlayerMarker(playerMarker, location);
            hasWinner = board.checkWinner(playerMarker);
            if (hasWinner) {
                System.out.println("Winner: Player '" + playerMarker + "'!");
            } else {
                movesAvailable = board.hasMovesAvailable();
                playerIndex = playerIndex + 1;
                if (playerIndex >= playerMarkers.size()) {
                    playerIndex = 0;
                }
            }
            System.out.println(board);
            System.out.println();
        }
        if (!hasWinner && !movesAvailable) {
           System.out.println("Tie game!"); 
        }
    }


}
