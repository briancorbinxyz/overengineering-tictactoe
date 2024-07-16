package org.example;

import java.util.List;

public class Game {

    private final GameBoard board;

    private final List<Player> players;

    private int dimension;

    public Game() {
        this(3);
    }

    public Game(int dimension) {
        this.dimension = dimension;
        this.board = new GameBoard(dimension);
        this.players = List.of(new BotPlayer("X"), new BotPlayer("O"));
    }

    public void play() throws InterruptedException {
        boolean hasWinner = false;
        boolean movesAvailable = board.hasMovesAvailable();
        int playerIndex = 0;
        System.out.println(board);
        System.out.println();
        while (!hasWinner && movesAvailable) {
            Thread.sleep(10);
            Player player = players.get(playerIndex);
            String playerMarker = player.getPlayerMarker();
            int location = player.nextMove(board);
            board.placePlayerMarker(playerMarker, location);
            hasWinner = board.checkWinner(playerMarker);
            if (hasWinner) {
                System.out.println("Winner: Player '" + playerMarker + "'!");
            } else {
                movesAvailable = board.hasMovesAvailable();
                playerIndex = playerIndex + 1;
                if (playerIndex >= players.size()) {
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
