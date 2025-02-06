package org.xxdc.oss.example;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.SequencedCollection;
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

  /**
   * Constructs a new {@link Game} instance with a 3x3 game board, persistence enabled, and a human
   * player as player 'X' and a bot player as player 'O'.
   */
  public Game() {
    this(
        3,
        true,
        new PlayerNode.Local<>("X", new HumanPlayer()),
        new PlayerNode.Local<>("O", new BotPlayer()));
  }

  /**
   * Constructs a new {@link Game} instance with the specified game board size, persistence enabled
   * state, and player nodes.
   *
   * @param size The size of the game board, e.g. 3 for a 3x3 board.
   * @param persistenceEnabled Whether game state should be persisted to a file.
   * @param players The player nodes for the game, which include the player marker and player
   *     implementation.
   */
  public Game(int size, boolean persistenceEnabled, PlayerNode... players) {
    this.playerNodes = PlayerNodes.of(players);
    this.gameId = UUID.randomUUID();
    this.moveNumber = 0;
    this.gameState = new ArrayDeque<>();
    this.gameState.add(
        new GameState(GameBoard.withDimension(size), this.playerNodes.playerMarkerList(), 0));
    this.persistenceEnabled = persistenceEnabled;
  }

  /**
   * Loads a {@link Game} instance from the specified file.
   *
   * @param gameFile The file containing the serialized {@link Game} instance.
   * @return The loaded {@link Game} instance.
   * @throws IOException If an I/O error occurs while reading the file.
   * @throws ClassNotFoundException If the serialized class cannot be found.
   */
  public static Game from(File gameFile) throws IOException, ClassNotFoundException {
    GamePersistence persistence = new GamePersistence();
    return persistence.loadFrom(gameFile);
  }

  /**
   * Constructs a new {@link Game} instance with a 3x3 game board, persistence disabled, and a bot
   * player as player 'X' and a bot player as player 'O'.
   */
  public static Game ofBots() {
    return new Game(
        3,
        false,
        new PlayerNode.Local<>("X", new BotPlayer()),
        new PlayerNode.Local<>("O", new BotPlayer()));
  }

  /**
   * Plays the game, rendering the board, applying player moves, and persisting the game state if
   * enabled. The game continues until a winning player is found or there are no more moves
   * available, at which point the winner or tie is logged.
   */
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

  /**
   * Returns the unique identifier for this game instance (Deprecated).
   *
   * @return the game ID
   * @deprecated use {@link #id()} instead
   */
  @Deprecated(since = "1.5.0", forRemoval = true)
  public UUID getGameId() {
    return gameId;
  }

  /**
   * Returns the unique identifier for this game instance.
   *
   * @return the game ID
   */
  public UUID id() {
    return gameId;
  }

  @Override
  public void close() throws Exception {
    playerNodes.close();
  }

  /**
   * Returns the number of players in the game.
   *
   * @return the number of players
   */
  public int numberOfPlayers() {
    return playerNodes.playerMarkerList().size();
  }

  /** Returns the history of the game, including all moves made. */
  public SequencedCollection<GameState> history() {
    return gameState;
  }

  private Optional<String> checkWon(GameState state) {
    return state.lastMove() > -1 && state.lastPlayerHasChain()
        ? Optional.of(state.playerMarkers().get(state.lastPlayerIndex()))
        : Optional.empty();
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

  private void renderBoard() {
    log.log(Level.INFO, "\n" + currentGameState().board());
  }

  private GameState currentGameState() {
    return gameState.peekLast();
  }
}
