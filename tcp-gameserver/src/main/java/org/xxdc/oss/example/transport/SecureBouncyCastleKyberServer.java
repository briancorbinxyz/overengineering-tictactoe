package org.xxdc.oss.example.transport;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.security.*;
import java.security.spec.InvalidParameterSpecException;
import javax.crypto.DecapsulateException;
import javax.crypto.KEM;
import javax.crypto.SecretKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import org.bouncycastle.pqc.jcajce.spec.KyberParameterSpec;
import org.xxdc.oss.example.security.KyberKEMProvider;

/**
 * Represents a secure message handler for the server side of a secure communication channel. This
 * class extends the `SecureMessageHandler` class and is responsible for initializing the secure
 * channel, exchanging the shared secret key with the client, and providing methods for sending and
 * receiving encrypted messages uses non-JDK Kyber for PQC.
 */
public final class SecureBouncyCastleKyberServer extends SecureDuplexMessageHandler {

  private static final System.Logger log =
      System.getLogger(MethodHandles.lookup().lookupClass().getName());

  private void registerSecurityProviders() {
    Security.addProvider(new BouncyCastleProvider());
    Security.addProvider(new BouncyCastlePQCProvider());
    Security.addProvider(new KyberKEMProvider());
  }

  /**
   * Constructs a new `SecureServerMessageHandler` instance with the given `RemoteMessageHandler`.
   *
   * @param remoteMessageHandler the `RemoteMessageHandler` to use for sending and receiving
   *     messages
   */
  public SecureBouncyCastleKyberServer(DuplexMessageHandler remoteMessageHandler) {
    super(remoteMessageHandler);
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
    } catch (NoSuchAlgorithmException
        | NoSuchProviderException
        | InvalidParameterSpecException
        | InvalidKeyException
        | InvalidAlgorithmParameterException
        | DecapsulateException e) {
      throw new IllegalArgumentException(
          "Invalid security configuration/exchange: " + e.getMessage(), e);
    }
  }

  /**
   * Exchanges the shared secret key with the client using the Kyber key encapsulation mechanism
   * (KEM).
   *
   * @return the shared secret key
   * @throws NoSuchAlgorithmException if the specified algorithm is not available
   * @throws IOException if there is an error during the key exchange process
   * @throws NoSuchProviderException if the specified provider is not available
   * @throws InvalidParameterSpecException if the parameter specification is invalid
   * @throws InvalidAlgorithmParameterException if the algorithm parameters are invalid
   * @throws InvalidKeyException if the key is invalid
   * @throws DecapsulateException if there is an error during the decapsulation process
   */
  protected SecretKey exchangeSharedKey()
      throws NoSuchAlgorithmException,
          IOException,
          NoSuchProviderException,
          InvalidParameterSpecException,
          InvalidAlgorithmParameterException,
          InvalidKeyException,
          DecapsulateException {
    var keyPair = generateKeyPair();
    publishKey(keyPair.getPublic());
    // Receiver side
    var encapsulated = handler.receiveBytes();
    var encapsulatedParams = handler.receiveBytes();
    var kem = KEM.getInstance("Kyber", "BCPQC.KEM");
    var params = AlgorithmParameters.getInstance("Kyber");
    params.init(encapsulatedParams);
    var paramSpec = params.getParameterSpec(KyberParameterSpec.class);
    var decapsulator = kem.newDecapsulator(keyPair.getPrivate(), paramSpec);
    return decapsulator.decapsulate(encapsulated);
  }

  /**
   * Generates a new Kyber key pair and publishes the public key to the client.
   *
   * @return the generated key pair
   * @throws NoSuchAlgorithmException if the specified algorithm is not available
   * @throws IOException if there is an error during the key publication process
   */
  private KeyPair generateKeyPair() throws NoSuchAlgorithmException, IOException {
    var keyPairGen = KeyPairGenerator.getInstance("Kyber");
    return keyPairGen.generateKeyPair();
  }

  /**
   * Publishes the given public key to the client.
   *
   * @param pk the public key to publish
   * @throws IOException if there is an error during the publication process
   */
  public void publishKey(PublicKey pk) throws IOException {
    handler.sendObject(pk);
  }
}
