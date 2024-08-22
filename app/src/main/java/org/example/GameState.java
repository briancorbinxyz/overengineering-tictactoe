package org.example;

import java.util.ArrayList;
import java.util.List;

public record GameState(
    GameBoard board, List<String> playerMarkers, int currentPlayerIndex, int lastMove)
    implements JsonSerializable {

  public GameState(GameBoard board, List<String> playerMarkers, int currentPlayerIndex) {
    this(board, playerMarkers, currentPlayerIndex, -1);
  }

  public GameState(GameState state) {
    this(
        state.board,
        new ArrayList<>(state.playerMarkers),
        state.currentPlayerIndex,
        state.lastMove);
  }

  public String currentPlayer() {
    return playerMarkers.get(currentPlayerIndex);
  }

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

  public GameState withMove(int move) {
    GameBoard newBoard = board.withMove(currentPlayer(), move);
    int newCurrentPlayerIndex = (currentPlayerIndex + 1) % playerMarkers.size();
    return new GameState(newBoard, playerMarkers, newCurrentPlayerIndex, move);
  }
}
