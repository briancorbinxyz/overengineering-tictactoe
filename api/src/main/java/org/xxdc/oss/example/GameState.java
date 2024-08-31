package org.xxdc.oss.example;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the current state of a game, including the game board, player markers, and the index
 * of the current player. Provides methods to interact with the game state, such as checking for
 * available moves, checking for chains, and advancing the game to the next player's turn.
 * Implements the JsonSerializable and Serializable interfaces, allowing the game state to be
 * serialized and deserialized.
 *
 * @param board The game board.
 * @param playerMarkers The list of player markers.
 * @param currentPlayerIndex The index of the current player in the {@code playerMarkers} list.
 * @param lastMove The index of the last move made on the game board.
 */
public record GameState(
    GameBoard board, List<String> playerMarkers, int currentPlayerIndex, int lastMove)
    implements JsonSerializable, Serializable {

  /**
   * Constructs a new {@link GameState} instance with the provided game board, player markers, and
   * the index of the current player. The last move index is set to -1 to indicate that no move has
   * been made yet.
   *
   * @param board The game board.
   * @param playerMarkers The list of player markers.
   * @param currentPlayerIndex The index of the current player in the {@code playerMarkers} list.
   */
  public GameState(GameBoard board, List<String> playerMarkers, int currentPlayerIndex) {
    this(board, playerMarkers, currentPlayerIndex, -1);
  }

  /**
   * Constructs a new {@link GameState} instance by copying the state from the provided {@link
   * GameState} object. This constructor creates a deep copy of the game board, player markers, and
   * current player index, allowing the new {@link GameState} instance to be modified independently
   * from the original.
   *
   * @param state The {@link GameState} object to copy.
   */
  public GameState(GameState state) {
    this(
        state.board,
        new ArrayList<>(state.playerMarkers),
        state.currentPlayerIndex,
        state.lastMove);
  }

  /**
   * Returns the current player's marker.
   *
   * @return The current player's marker.
   */
  public String currentPlayer() {
    return playerMarkers.get(currentPlayerIndex);
  }

  /**
   * Checks if there are any available moves on the game board.
   *
   * @return {@code true} if there are available moves, {@code false} otherwise.
   */
  public boolean hasMovesAvailable() {
    return board.hasMovesAvailable();
  }

  /**
   * Checks if the specified player has a chain on the game board.
   *
   * @param player The player marker to check for a chain.
   * @return {@code true} if the player has a chain, {@code false} otherwise.
   */
  public boolean hasChain(String player) {
    return board.hasChain(player);
  }

  /**
   * Returns a list of available moves on the game board.
   *
   * @return A list of available moves on the game board.
   */
  public List<Integer> availableMoves() {
    return board.availableMoves();
  }

  /**
   * Checks if the game is in a terminal state, where either there are no more available moves or a
   * player has a chain.
   *
   * @return {@code true} if the game is in a terminal state, {@code false} otherwise.
   */
  public boolean isTerminal() {
    if (!board.hasMovesAvailable()) {
      return true;
    }
    for (String player : playerMarkers) {
      if (board.hasChain(player)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String asJsonString() {
    StringBuilder json = new StringBuilder();
    json.append("{");
    if (playerMarkers.size() > 0) {
      json.append("\"playerMarkers\":[\"")
          .append(String.join("\",\"", playerMarkers))
          .append("\"],");
    } else {
      json.append("\"playerMarkers\":[],");
    }
    json.append("\"currentPlayerIndex\":").append(currentPlayerIndex).append(",");
    json.append("\"board\":").append(board.asJsonString());
    json.append("}");
    return json.toString();
  }

  /**
   * Creates a new {@link GameState} instance with the current player's move applied to the game
   * board.
   *
   * @param move The move to apply to the game board.
   * @return A new {@link GameState} instance with the updated game board and current player index.
   */
  public GameState afterPlayerMoves(int move) {
    GameBoard newBoard = board.withMove(currentPlayer(), move);
    int newCurrentPlayerIndex = (currentPlayerIndex + 1) % playerMarkers.size();
    return new GameState(newBoard, playerMarkers, newCurrentPlayerIndex, move);
  }

  /**
   * Checks if the last player to move has a chain on the game board.
   *
   * @return {@code true} if the last player has a chain, {@code false} otherwise.
   */
  public boolean lastPlayerHasChain() {
    return board.hasChain(lastPlayer());
  }

  /**
   * Returns the last player's marker from the list of player markers.
   *
   * @return the last player's marker
   * @throws GameServiceException if there is no last move or the board is empty
   */
  private String lastPlayer() {
    if (lastMove < 0 || board.isEmpty()) {
      throw new GameServiceException("null last player");
    }
    return playerMarkers.get(lastPlayerIndex());
  }

  /**
   * Returns the index of the last player to move in the list of player markers.
   *
   * @return the index of the last player to move in the list of player markers
   */
  public int lastPlayerIndex() {
    return (currentPlayerIndex + playerMarkers.size() - 1) % playerMarkers.size();
  }
}
