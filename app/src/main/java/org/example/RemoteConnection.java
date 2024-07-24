package org.example;

import java.io.IOException;

public interface RemoteConnection {

    public void sendMessage(String message) throws IOException;

    public String receiveMessage() throws IOException;

}
