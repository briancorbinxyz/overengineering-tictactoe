package org.example;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.UUID;

/**
 * Represents a game of Tic-Tac-Toe, including the game board, players, and game state.
 * The game can be serialized and persisted to a file, and loaded from a file.
 * The game can be played by alternating moves between human and bot players.
 */
public class Game implements Serializable, AutoCloseable {

    private static final long serialVersionUID = 1L;

    private final UUID gameId;

    private final GameBoard board;

    private final PlayerList players;

    private int moveNumber;

    public Game() {
        this(3);
    }

    public Game(int dimension) {
        this.board = new GameBoard(dimension);
        this.players = PlayerList.of(
            new HumanPlayer("X"),
            new BotPlayer("O")
        );
        this.gameId = UUID.randomUUID();
        this.moveNumber = 0;
    }

    public static Game from(File gameFile) throws IOException, ClassNotFoundException {
        GamePersistence persistence = new GamePersistence();
        return persistence.loadFrom(gameFile);
    }

    public void play() throws Exception {
        GamePersistence persistence = new GamePersistence();
        File persistenceDir = Files.createTempDirectory(String.valueOf(gameId)).toFile();
        boolean hasWinner = false;
        boolean movesAvailable = board.hasMovesAvailable();
        players.render();
        while (!hasWinner && movesAvailable) {
            renderBoard();
            moveNumber = moveNumber + 1;
            Player player = players.nextPlayer();
            String playerMarker = player.getPlayerMarker();
            int location = player.nextMove(board);
            board.placePlayerMarker(playerMarker, location);
            hasWinner = board.checkWinner(playerMarker);
            if (hasWinner) {
                System.out.println("Winner: Player '" + playerMarker + "'!");
            } else {
                movesAvailable = board.hasMovesAvailable();
            }
            persistence.saveTo(new File(persistenceDir, String.valueOf(gameId) + "." + moveNumber + ".game"), this);
        }
        if (!hasWinner && !movesAvailable) {
           System.out.println("Tie game!"); 
        }
        renderBoard();
    }

    public UUID getGameId() {
        return gameId;
    }

    private void renderBoard() {
        System.out.println(board);
        System.out.println();
    }

    @Override
    public void close() throws Exception {
        players.close();
    }


}
