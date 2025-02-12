package org.xxdc.oss.example.transport;

import static org.testng.Assert.*;

import java.io.*;
import org.testng.annotations.Test;

public class SecureModuleLatticeTest {

  @Test
  public void test_secure_key_exchange() throws IOException {
    // client -> server
    var clientOut = new PipedOutputStream();
    var serverIn = new PipedInputStream(clientOut);

    // server -> client
    var serverOut = new PipedOutputStream();
    var clientIn = new PipedInputStream(serverOut);

    var server =
        new SecureModuleLatticeServer(
            new DuplexMessageHandler(
                new ObjectOutputStream(serverOut), new ObjectInputStream(serverIn)));
    var client =
        new SecureModuleLatticeClient(
            new DuplexMessageHandler(
                new ObjectOutputStream(clientOut), new ObjectInputStream(clientIn)));

    server.init();
    client.init();

    server.sendMessage("The revolution will be televised.");
    assertEquals(client.receiveMessage(), "The revolution will be televised");
  }
}
