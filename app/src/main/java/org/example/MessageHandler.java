package org.example;

import java.io.IOException;

public interface MessageHandler extends AutoCloseable {

    /**
     * Initializes the SecureMessageHandler implementation. This method must be called before any
     * other methods can be used.
     *
     * @throws IOException if there is an error during the initialization process
     */
    void init() throws IOException;

    void sendMessage(String message) throws IOException;

    String receiveMessage() throws IOException;
}
