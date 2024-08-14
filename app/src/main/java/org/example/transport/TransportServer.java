package org.example.transport;

import org.example.GameBoard;

public interface TransportServer extends AutoCloseable {

    public void initialize(TransportConfiguration config);

    public void send(GameBoard board);

    public int accept(GameBoard board);
}
