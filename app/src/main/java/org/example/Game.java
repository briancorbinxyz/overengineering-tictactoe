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

  private final Deque<GameState> gameState;

  private final PlayerNodes playerNodes;

  private final boolean persistenceEnabled;

  private int moveNumber;

  public Game() {
    this(
        3,
        true,
        new PlayerNode.Local<>("X", new HumanPlayer()),
        new PlayerNode.Local<>("O", new BotPlayer()));
  }

  public Game(int size, boolean persistenceEnabled, PlayerNode... players) {
    this.playerNodes = PlayerNodes.of(players);
    this.gameId = UUID.randomUUID();
    this.moveNumber = 0;
    this.gameState = new ArrayDeque<>();
    this.gameState.add(new GameState(GameBoard.with(size), this.playerNodes.playerMarkerList(), 0));
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
      GameState state = currentGameState();
      boolean movesAvailable = state.hasMovesAvailable();
      PlayerNode currentPlayer = playerNodes.byIndex(state.currentPlayerIndex());
      Optional<String> winningPlayer = checkWon(state);

      // Print Initial Setup
      playerNodes.render();
      while (winningPlayer.isEmpty() && movesAvailable) {
        renderBoard();
        log.log(Level.DEBUG, "Current Player: {0}", currentPlayer.playerMarker());
        moveNumber += 1;
        var newState = state.afterPlayerMoves(currentPlayer.applyAsInt(state));
        state = pushGameState(newState);
        if (persistenceEnabled && state.board() instanceof Serializable) {
          persistence.saveTo(gameFile(persistenceDir), this);
        }
        winningPlayer = checkWon(state);
        movesAvailable = state.hasMovesAvailable();
        currentPlayer = playerNodes.byIndex(state.currentPlayerIndex());
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

  private Optional<String> checkWon(GameState state) {
    return state.lastMove() > -1 && state.lastPlayerHasChain() ? Optional.of(state.playerMarkers().get(state.lastPlayerIndex())) : Optional.empty();
  }

  private File gameFileDirectory() throws IOException {
    return Files.createTempDirectory(String.valueOf(gameId)).toFile();
  }

  private File gameFile(File persistenceDir) {
    return new File(persistenceDir, String.valueOf(gameId) + "." + moveNumber + ".game");
  }

  private GameState pushGameState(GameState state) {
    gameState.add(state);
    return state;
  }

  public UUID getGameId() {
    return gameId;
  }

  private void renderBoard() {
    log.log(Level.INFO, "\n" + currentGameState().board());
  }

  private GameState currentGameState() {
    return gameState.peekLast();
  }

  @Override
  public void close() throws Exception {
    playerNodes.close();
  }
}
