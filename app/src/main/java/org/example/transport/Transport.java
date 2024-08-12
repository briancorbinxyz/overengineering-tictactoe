package org.example.transport;

import org.example.GameBoard;

public interface Transport extends AutoCloseable {

    public void initialize(TransportConfiguration config) throws Exception;

    public void sendState(GameBoard board);

    public int acceptMove(GameBoard board);

}
