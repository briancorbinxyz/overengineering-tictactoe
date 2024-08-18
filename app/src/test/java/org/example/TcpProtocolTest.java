package org.example;

import java.util.regex.Matcher;
import org.example.transport.tcp.TcpProtocol;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TcpProtocolTest {

    @Test
    public void testNextMoveRegexMatchesNextMoveMessage() {
        String msg =
                "{\"version\":1,\"message\":\"nextMove\",\"state\":{\"playerMarkers\":[\"X\",\"O\"],\"currentPlayerIndex\":1,\"board\":{\"dimension\":3,\"content\":[\"X\",null,\"O\",null,\"X\",null,\"X\",null,\"O\"]}}}";
        Matcher matcher = TcpProtocol.NEXT_MOVE_JSON_PATTERN.matcher(msg);
        Assert.assertTrue(matcher.matches());
    }
}
