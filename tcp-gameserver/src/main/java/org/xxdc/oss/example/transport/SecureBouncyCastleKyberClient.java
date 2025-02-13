package org.xxdc.oss.example.transport;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.security.*;
import javax.crypto.KEM;
import javax.crypto.SecretKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import org.bouncycastle.pqc.jcajce.spec.KyberParameterSpec;
import org.xxdc.oss.example.security.KyberKEMProvider;

/**
 * Represents a secure message handler for the client side of a secure communication channel. This
 * class extends the `SecureMessageHandler` class and is responsible for initializing the secure
 * channel, exchanging the shared secret key with the server, and providing methods for sending and
 * receiving encrypted messages. Uses non-JDK Kyber for PQC.
 */
public final class SecureBouncyCastleKyberClient extends SecureDuplexMessageHandler {

  private static final System.Logger log =
      System.getLogger(MethodHandles.lookup().lookupClass().getName());

  private void registerSecurityProviders() {
    Security.addProvider(new BouncyCastleProvider());
    Security.addProvider(new BouncyCastlePQCProvider());
    Security.addProvider(new KyberKEMProvider());
  }

  /**
   * Constructs a new `SecureClientMessageHandler` instance with the given `RemoteMessageHandler`.
   *
   * @param handler the `RemoteMessageHandler` to use for the secure communication channel
   */
  public SecureBouncyCastleKyberClient(DuplexMessageHandler handler) {
    super(handler);
    registerSecurityProviders();
  }

  /**
   * Initializes the secure message handler by setting up the secure channel and exchanging the
   * shared secret key with the client.
   *
   * @throws IOException if there is an error during the initialization process
   */
  @Override
  public void init() throws IOException {
    // Sender side
    try {
      handler.init();
      log.log(
          System.Logger.Level.DEBUG,
          "Initializing secure channel for {0}. Exchanging shared key...",
          getClass().getSimpleName());
      sharedKey = exchangeSharedKey();
      initialized = true;
      log.log(
          System.Logger.Level.DEBUG,
          "Secure connection for {0} established with {1} shared key.",
          getClass().getSimpleName(),
          sharedKey.getAlgorithm());
    } catch (ClassNotFoundException
        | IOException
        | NoSuchAlgorithmException
        | NoSuchProviderException
        | InvalidKeyException
        | InvalidAlgorithmParameterException e) {
      throw new IllegalArgumentException(
          "Invalid security configuration/exchange: " + e.getMessage(), e);
    }
  }

  /**
   * Initializes the secure message handler by exchanging a shared key with the remote party. This
   * method is called on the sender side to set up the secure communication channel.
   *
   * @throws IOException if there is an error initializing the communication handler
   * @throws ClassNotFoundException if the remote public key class cannot be found
   * @throws NoSuchAlgorithmException if the specified key exchange algorithm is not available
   * @throws NoSuchProviderException if the specified cryptographic provider is not available
   * @throws InvalidKeyException if the remote public key is invalid
   * @throws InvalidAlgorithmParameterException if the key exchange parameters are invalid
   */
  protected SecretKey exchangeSharedKey()
      throws NoSuchAlgorithmException,
          NoSuchProviderException,
          ClassNotFoundException,
          IOException,
          InvalidAlgorithmParameterException,
          InvalidKeyException {
    var kem = KEM.getInstance("Kyber", "BCPQC.KEM");
    var publicKey = retrieveKey();
    var paramSpec = KyberParameterSpec.kyber1024;
    var encapsulator = kem.newEncapsulator(publicKey, paramSpec, null);
    var encapsulated = encapsulator.encapsulate();
    handler.sendBytes(encapsulated.encapsulation());
    handler.sendBytes(encapsulated.params());
    return encapsulated.key();
  }

  /**
   * Retrieves the public key from the server
   *
   * @return the public key from the server
   * @throws ClassNotFoundException if the class is not found whilst deserializing from the message
   *     handler
   * @throws IOException if there is an error with the communications channel
   */
  private PublicKey retrieveKey() throws ClassNotFoundException, IOException {
    return (PublicKey) handler.receiveObject();
  }
}
