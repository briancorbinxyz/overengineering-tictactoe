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
public class Game implements Serializable, AutoCloseable {

  private static final Logger log = System.getLogger(Game.class.getName());

  private static final long serialVersionUID = 1L;

  private final UUID gameId;

  private final Deque<GameBoard> boards;

  private final PlayerNodes players;

  private final boolean persistenceEnabled;

  private int currentPlayerIdx;

  private int moveNumber;

  public Game() {
    this(
        3,
        true,
        new PlayerNode.Local<>("X", new HumanPlayer()),
        new PlayerNode.Local<>("O", new BotPlayer()));
  }

  public Game(int size, boolean persistenceEnabled, PlayerNode... players) {
    this.boards = new ArrayDeque<>();
    this.boards.add(new GameBoardNativeImpl(size));
    this.players = PlayerNodes.of(players);
    this.gameId = UUID.randomUUID();
    this.moveNumber = 0;
    this.currentPlayerIdx = 0;
    this.persistenceEnabled = persistenceEnabled;
  }

  public static Game from(File gameFile) throws IOException, ClassNotFoundException {
    GamePersistence persistence = new GamePersistence();
    return persistence.loadFrom(gameFile);
  }

  public void play() {
    try {
      GamePersistence persistence = new GamePersistence();
      File persistenceDir = gameFileDirectory();
      GameBoard board = activeGameBoard();
      boolean movesAvailable = board.hasMovesAvailable();
      PlayerNode currentPlayer = players.byIndex(currentPlayerIdx);
      Optional<String> winningPlayer = checkWon(board, currentPlayer.playerMarker());

      // Print Initial Setup
      players.render();
      while (winningPlayer.isEmpty() && movesAvailable) {
        renderBoard();
        log.log(Level.DEBUG, "Current Player: {0}", currentPlayer.playerMarker());
        moveNumber += 1;
        var state = new GameState(board, players.playerMarkerList(), currentPlayerIdx);
        var newBoard =
            board.withMove(currentPlayer.playerMarker(), currentPlayer.applyAsInt(state));
        board = pushGameBoard(newBoard);
        if (persistenceEnabled && board instanceof Serializable) {
          persistence.saveTo(gameFile(persistenceDir), this);
        }
        winningPlayer = checkWon(board, currentPlayer.playerMarker());
        movesAvailable = board.hasMovesAvailable();
        currentPlayerIdx = players.nextPlayerIndex(currentPlayerIdx);
        currentPlayer = players.byIndex(currentPlayerIdx);
      }

      winningPlayer.ifPresentOrElse(
          p -> log.log(Level.INFO, "Winner: Player {0}!", p),
          () -> log.log(Level.INFO, "Tie Game!"));
      renderBoard();
      close();
    } catch (Exception e) {
      throw new GameServiceException("Failure whilst playing game: " + e.getMessage(), e);
    }
  }

  private Optional<String> checkWon(GameBoard board, String playerMarker) {
    return board.hasChain(playerMarker) ? Optional.of(playerMarker) : Optional.empty();
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

  @Override
  public void close() throws Exception {
    players.close();
  }
}
