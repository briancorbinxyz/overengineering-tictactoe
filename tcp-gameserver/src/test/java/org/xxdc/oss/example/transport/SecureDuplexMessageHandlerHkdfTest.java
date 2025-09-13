package org.xxdc.oss.example.transport;

import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidParameterSpecException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.crypto.DecapsulateException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.testng.annotations.Test;

/**
 * Tests that SecureDuplexMessageHandler uses HKDF (JEP 510) derived AES keys to successfully
 * encrypt/decrypt messages over the duplex channel. This isolates the KDF change by injecting a
 * fixed KEM shared secret rather than running a full Kyber handshake.
 */
public class SecureDuplexMessageHandlerHkdfTest {

  private static final class LocalSecureHandler extends SecureDuplexMessageHandler {
    LocalSecureHandler(DuplexMessageHandler handler, SecretKey shared) {
      super(handler);
      this.sharedKey = shared; // simulate post-KEM shared secret
      this.initialized = true; // simulate completed init
      handler.init();
    }

    @Override
    public void init() throws IOException {
      // Already initialized in constructor for this test
    }

    @Override
    protected SecretKey exchangeSharedKey()
        throws NoSuchAlgorithmException,
            IOException,
            NoSuchProviderException,
            InvalidParameterSpecException,
            InvalidAlgorithmParameterException,
            InvalidKeyException,
            DecapsulateException,
            ClassNotFoundException {
      throw new UnsupportedOperationException("Not used in this test");
    }
  }

  @Test
  public void test_hkdf_based_send_receive_roundtrip() throws Exception {
    // Build a deterministic pseudo KEM-shared secret (IKM)
    byte[] ikm = new byte[32];
    for (int i = 0; i < ikm.length; i++) {
      ikm[i] = (byte) (i + 1);
    }
    SecretKey kemShared = new SecretKeySpec(ikm, "HKDF-IKM");

    // client -> server pipe
    var clientOut = new PipedOutputStream();
    var serverIn = new PipedInputStream(clientOut);

    // server -> client pipe
    var serverOut = new PipedOutputStream();
    var clientIn = new PipedInputStream(serverOut);

    // Defer creating client-side Object streams until server is ready to avoid header deadlocks

    // Start server in a separate thread (mirrors pattern used in SecureKyberTest)
    final String plaintext = "HKDF makes better keys than slicing bytes.";
    CountDownLatch serverReady = new CountDownLatch(1);
    Thread serverThread =
        new Thread(
            () -> {
              try {
                // Server: create OOS first and flush header, then signal readiness, then create OIS
                ObjectOutputStream serverOOS = new ObjectOutputStream(serverOut);
                serverOOS.flush();
                serverReady.countDown();
                ObjectInputStream serverOIS = new ObjectInputStream(serverIn);

                var serverDuplex = new DuplexMessageHandler(serverOOS, serverOIS);
                var server = new LocalSecureHandler(serverDuplex, kemShared);
                server.sendMessage(plaintext);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            });
    serverThread.start();

    // Client runs on current thread — construct OOS, then wait for server header, then OIS
    ObjectOutputStream clientOOS = new ObjectOutputStream(clientOut);
    clientOOS.flush();
    // Wait for server to be ready to send (and to have flushed its OOS header)
    if (!serverReady.await(2, TimeUnit.SECONDS)) {
      throw new AssertionError("Server did not become ready in time");
    }
    ObjectInputStream clientOIS = new ObjectInputStream(clientIn);

    var clientDuplex = new DuplexMessageHandler(clientOOS, clientOIS);
    var client = new LocalSecureHandler(clientDuplex, kemShared);
    String received = client.receiveMessage();

    assertEquals(received, plaintext);

    // Ensure server thread finishes
    serverThread.join(5_000);
  }
}
