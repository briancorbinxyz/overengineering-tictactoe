package org.example;

import java.util.regex.Matcher;
import java.util.List;
import org.example.transport.tcp.TcpProtocol;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

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
}
