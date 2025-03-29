package org.xxdc.oss.example.transport;

import static org.testng.Assert.*;

import java.io.*;
import org.testng.annotations.Test;

public class SecureKyberTest {

  @Test
  public void test_secure_key_exchange() throws IOException {
    // client -> server
    var clientOut = new PipedOutputStream();
    var serverIn = new PipedInputStream(clientOut);

    // server -> client
    var serverOut = new PipedOutputStream();
    var clientIn = new PipedInputStream(serverOut);

    new Thread(
            () -> {
              try {
                var server =
                    new SecureKyberServer(
                        new DuplexMessageHandler(
                            new ObjectOutputStream(serverOut), new ObjectInputStream(serverIn)));
                server.init();
                server.sendMessage(
                    "Different denotes neither bad nor good, but it certainly means not the same.");
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            })
        .start();

    var client =
        new SecureKyberClient(
            new DuplexMessageHandler(
                new ObjectOutputStream(clientOut), new ObjectInputStream(clientIn)));

    client.init();

    assertEquals(
        client.receiveMessage(),
        "Different denotes neither bad nor good, but it certainly means not the same.");
  }
}
