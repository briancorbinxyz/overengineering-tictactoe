package org.xxdc.oss.example.transport.tcp;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.regex.Matcher;
import org.testng.annotations.Test;

public class TcpProtocolTest {

  @Test
  public void testNextMoveRegexMatchesNextMoveMessage() {
    String msg =
        "{\"version\":1,\"message\":\"nextMove\",\"state\":{\"playerMarkers\":[\"X\",\"O\"],\"currentPlayerIndex\":1,\"board\":{\"dimension\":3,\"content\":[\"X\",null,\"O\",null,\"X\",null,\"X\",null,\"O\"]}}}";
    Matcher matcher = TcpProtocol.NEXT_MOVE_JSON_PATTERN.matcher(msg);
    assertTrue(matcher.matches());
  }

  @Test
  public void testCanRetrieveGameStateFromNextMoveMessage() {
    String msg =
        "{\"version\":1,\"message\":\"nextMove\",\"state\":{\"playerMarkers\":[\"X\",\"O\"],\"currentPlayerIndex\":1,\"board\":{\"dimension\":3,\"content\":[\"X\",null,\"O\",null,\"X\",null,\"X\",null,\"O\"]}}}";
    var maybeState = TcpProtocol.fromNextMoveState(msg);
    assertTrue(maybeState.isPresent());
    var state = maybeState.get();
    assertEquals(state.currentPlayer(), "O");
    assertEquals(state.playerMarkers().size(), 2);
    assertEquals(state.playerMarkers(), List.of("X", "O"));
    assertEquals(state.board().dimension(), 3);
    assertEquals(state.board().availableMoves(), List.of(1, 3, 5, 7));
  }

  // --- T019: Protocol v2 chain length tests ---

  @Test
  public void testV2StartMessageIncludesChainLength() {
    String msg = String.format(TcpProtocol.GAME_STARTED_JSON_FORMAT, "X", 3);
    assertTrue(msg.contains("\"chainLength\":3"));
    assertTrue(msg.contains("\"version\":2"));
    var marker = TcpProtocol.fromGameStartedState(msg);
    assertTrue(marker.isPresent());
    assertEquals(marker.get(), "X");
  }

  @Test
  public void testV2StartMessageChainLengthParsing() {
    String msg = String.format(TcpProtocol.GAME_STARTED_JSON_FORMAT, "O", 4);
    int chainLength = TcpProtocol.chainLengthFromStartMessage(msg, 5);
    assertEquals(chainLength, 4);
  }

  @Test
  public void testV1StartMessageDefaultsChainLengthToDimension() {
    String v1Msg = "{\"version\":1,\"message\":\"start\",\"assignedPlayerMarker\":\"X\"}";
    int chainLength = TcpProtocol.chainLengthFromStartMessage(v1Msg, 3);
    assertEquals(chainLength, 3);
  }

  @Test
  public void testV2NextMoveMessageIncludesChainLength() {
    var board = org.xxdc.oss.example.GameBoard.withDimension(5, 3);
    board = board.withMove("X", 0);
    var state = new org.xxdc.oss.example.GameState(board, List.of("X", "O"), 1);
    String msg = String.format(TcpProtocol.NEXT_MOVE_JSON_FORMAT, state.asJsonString());
    assertTrue(msg.contains("\"chainLength\":3"));
    assertTrue(msg.contains("\"version\":2"));
  }

  @Test
  public void testV2NextMoveRoundTrip() {
    var board = org.xxdc.oss.example.GameBoard.withDimension(5, 3);
    board = board.withMove("X", 0).withMove("O", 1);
    var state = new org.xxdc.oss.example.GameState(board, List.of("X", "O"), 0);
    String msg = String.format(TcpProtocol.NEXT_MOVE_JSON_FORMAT, state.asJsonString());
    var parsed = TcpProtocol.fromNextMoveState(msg);
    assertTrue(parsed.isPresent());
    assertEquals(parsed.get().board().dimension(), 5);
    assertEquals(parsed.get().board().chainLength(), 3);
    assertEquals(parsed.get().currentPlayer(), "X");
  }

  @Test
  public void testV1NextMoveMessageDefaultsChainLengthToDimension() {
    String v1Msg =
        "{\"version\":1,\"message\":\"nextMove\",\"state\":{\"playerMarkers\":[\"X\",\"O\"],\"currentPlayerIndex\":0,\"board\":{\"dimension\":3,\"content\":[\"X\",null,null,null,null,null,null,null,null]}}}";
    var parsed = TcpProtocol.fromNextMoveState(v1Msg);
    assertTrue(parsed.isPresent());
    assertEquals(parsed.get().board().chainLength(), 3);
  }
}
