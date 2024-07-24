package org.example;

import java.io.IOException;

public interface MessageHandler {

    void init() throws IOException; 

    void sendMessage(String message) throws IOException;

    String receiveMessage() throws IOException;

}
