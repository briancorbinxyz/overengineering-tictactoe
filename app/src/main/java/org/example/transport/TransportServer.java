package org.example.transport;

import org.example.GameBoard;

public interface TransportServer extends AutoCloseable {

    public void initialize(TransportConfiguration config);

    public void sendState(GameBoard board);

    public int acceptMove(GameBoard board);
}
