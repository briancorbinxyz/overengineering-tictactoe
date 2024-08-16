package org.example;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.regex.Matcher;

import org.example.transport.tcp.TcpProtocol;

public class TcpProtocolTest {

    @Test
    public void testNextMoveRegexMatchesNextMoveMessage() {
        String msg = "{\"version\":1,\"message\":\"nextMove\",\"board\":{\"dimension\":3,\"content\":[\"X\",null,\"O\",null,\"X\",null,\"X\",null,\"O\"]}}";
        Matcher matcher = TcpProtocol.NEXT_MOVE_JSON_PATTERN.matcher(msg);
        Assert.assertTrue(matcher.matches());
    }

}
