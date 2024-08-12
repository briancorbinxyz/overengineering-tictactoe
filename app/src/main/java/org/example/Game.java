package org.example;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents a game of Tic-Tac-Toe, including the game board, players, and game state. The game can
 * be serialized and persisted to a file, and loaded from a file. The game can be played by
 * alternating moves between human and bot players.
 */
public class Game implements Serializable {

    private static final Logger log = System.getLogger(Game.class.getName());

    private static final long serialVersionUID = 1L;

    private final UUID gameId;

    private final Deque<GameBoard> boards;

    private final Players players;

    private final boolean persistenceEnabled;

    private int currentPlayerIdx;

    private int moveNumber;

    public Game() {
        this(3, true, new HumanPlayer("X"), new BotPlayer("O"));
    }

    public Game(int size, boolean persistenceEnabled, Player... players) {
        this.boards = new ArrayDeque<>();
        this.boards.add(new GameBoardNativeImpl(size));
        this.players = Players.of(players);
        this.gameId = UUID.randomUUID();
        this.moveNumber = 0;
        this.currentPlayerIdx = 0;
        this.persistenceEnabled = persistenceEnabled;
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
        Player currentPlayer = players.byIndex(currentPlayerIdx);
        Optional<Player> winningPlayer = checkWon(board, currentPlayer);

        // Print Initial Setup
        players.render();
        while (winningPlayer.isEmpty() && movesAvailable) {
            renderBoard();
            moveNumber += 1;
            board =
                    pushGameBoard(
                            board.withMove(
                                    currentPlayer.playerMarker(),
                                    currentPlayer.nextMove(board)));
            if (persistenceEnabled && board instanceof Serializable) {
                persistence.saveTo(gameFile(persistenceDir), this);
            }
            winningPlayer = checkWon(board, currentPlayer);
            movesAvailable = board.hasMovesAvailable();
            currentPlayerIdx = players.nextPlayerIndex(currentPlayerIdx);
            currentPlayer = players.byIndex(currentPlayerIdx);
        }

        winningPlayer.ifPresentOrElse(
                player -> log.log(Level.INFO, "Winner: Player {0}!", player.playerMarker()),
                () -> log.log(Level.INFO, "Tie Game!"));
        renderBoard();
    }

    private Optional<Player> checkWon(GameBoard board, Player player) {
        return board.hasChain(player.playerMarker()) ? Optional.of(player) : Optional.empty();
    }

    private File gameFileDirectory() throws IOException {
        return Files.createTempDirectory(String.valueOf(gameId)).toFile();
    }

    private File gameFile(File persistenceDir) {
        return new File(persistenceDir, String.valueOf(gameId) + "." + moveNumber + ".game");
    }

    private GameBoard pushGameBoard(GameBoard board) {
        boards.add(board);
        return board;
    }

    public UUID getGameId() {
        return gameId;
    }

    private void renderBoard() {
        log.log(Level.INFO, "\n" + activeGameBoard());
    }

    private GameBoard activeGameBoard() {
        return boards.peekLast();
    }
}
