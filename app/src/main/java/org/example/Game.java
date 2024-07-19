package org.example;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.UUID;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.Optional;

/**
 * Represents a game of Tic-Tac-Toe, including the game board, players, and game state.
 * The game can be serialized and persisted to a file, and loaded from a file.
 * The game can be played by alternating moves between human and bot players.
 */
public class Game implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID gameId;

    private final Deque<GameBoard> boards;

    private final Players players;

    private Player currentPlayer;

    private int moveNumber;

    public Game() {
        this(3);
    }

    public Game(int size) {
        this.boards = new ArrayDeque<>();
        this.boards.add(new GameBoard(size));
        this.players = Players.of(
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
        File persistenceDir = gameFileDirectory();
        GameBoard board = activeGameBoard();
        boolean movesAvailable = board.hasMovesAvailable();
        currentPlayer = players.nextPlayer();
        Optional<Player> winningPlayer = checkWon(board, currentPlayer);

        // Print Initial Setup
        players.render();
        while (winningPlayer.isEmpty() && movesAvailable);
        { 
            renderBoard();
            moveNumber = moveNumber + 1;
            int location = currentPlayer.nextMove(board);
            board = pushGameBoard(board.withMove(currentPlayer.getPlayerMarker(), location));
            persistence.saveTo(new File(persistenceDir, String.valueOf(gameId) + "." + moveNumber + ".game"), this);
            winningPlayer = checkWon(board, currentPlayer);
            movesAvailable = board.hasMovesAvailable();
            currentPlayer = players.nextPlayer();
        };

        if (!winningPlayer.isEmpty() && !movesAvailable) {
           System.out.println("Tie game!"); 
        }
        winningPlayer.ifPresent((player) -> System.out.println("Winner: Player '" + player.getPlayerMarker() + "'!"));
        renderBoard();
    }

    private Optional<Player> checkWon(GameBoard board, Player player) {
        return board.hasChain(player.getPlayerMarker())
            ? Optional.of(player)
            : Optional.empty();
    }

    private File gameFileDirectory() throws IOException {
        return Files.createTempDirectory(String.valueOf(gameId)).toFile();
    }

    private GameBoard pushGameBoard(GameBoard board) {
        boards.add(board);
        return board;
    }

    public UUID getGameId() {
        return gameId;
    }

    private void renderBoard() {
        System.out.println(activeGameBoard());
        System.out.println();
    }

    private GameBoard activeGameBoard() {
        return boards.peekLast();
    }


}
