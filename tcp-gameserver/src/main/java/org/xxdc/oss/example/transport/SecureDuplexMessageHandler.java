package org.xxdc.oss.example.transport;

import java.io.IOException;
import java.lang.System.Logger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.spec.InvalidParameterSpecException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.DecapsulateException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public abstract class SecureDuplexMessageHandler implements MessageHandler {

  private static final Logger log = System.getLogger(SecureDuplexMessageHandler.class.getName());

  protected final DuplexMessageHandler handler;

  protected SecretKey sharedKey;

  protected boolean initialized = false;

  public SecureDuplexMessageHandler(DuplexMessageHandler handler) {
    this.handler = handler;
  }

  /**
   * Initializes the SecureMessageHandler implementation. This method must be called before any
   * other methods can be used.
   *
   * @throws IOException if there is an error during the initialization process
   */
  @Override
  public abstract void init() throws IOException;

  /**
   * Exchange a shared secret key with remote connection using the Kyber Key Encapsulation Mechanism
   * (KEM).
   *
   * <p>This method generates a new Kyber key pair, publishes the public key to the remote handler,
   * and then receives an encapsulated shared secret from the remote handler. It uses the private
   * key to decapsulate the shared secret.
   *
   * @return the shared secret key
   * @throws NoSuchAlgorithmException
   * @throws IllegalArgumentException NoSuchAlgorithmException if the "Kyber" algorithm is not
   *     available
   * @throws IOException if there is an error communicating with the remote handler
   * @throws NoSuchProviderException if the "BCPQC.KEM" provider is not available
   * @throws InvalidParameterSpecException if the received Kyber parameters are invalid
   * @throws InvalidAlgorithmParameterException if the Kyber parameters are invalid
   * @throws InvalidKeyException if the public/private key is invalid
   * @throws DecapsulateException if the decapsulation of the shared secret fails
   * @throws ClassNotFoundException
   */
  protected abstract SecretKey exchangeSharedKey()
      throws NoSuchAlgorithmException,
          IOException,
          NoSuchProviderException,
          InvalidParameterSpecException,
          InvalidAlgorithmParameterException,
          InvalidKeyException,
          DecapsulateException,
          ClassNotFoundException;

  @Override
  public void sendMessage(String message) throws IOException {
    checkInitialized();
    try {
      var cipher = newCipherInstance();
      var iv = new byte[12];
      var random = new SecureRandom();
      random.nextBytes(iv);
      var ivSpec = new GCMParameterSpec(128, iv);
      // Derive AES key (use only first 16 or 32 bytes for AES-128/AES-256)
      var aesKey = new SecretKeySpec(sharedKey.getEncoded(), 0, 32, "AES");
      cipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec);
      var ciphertext = cipher.doFinal(message.getBytes());
      var ivAndCiphertext = new byte[iv.length + ciphertext.length];
      System.arraycopy(iv, 0, ivAndCiphertext, 0, iv.length);
      System.arraycopy(ciphertext, 0, ivAndCiphertext, iv.length, ciphertext.length);
      handler.sendBytes(ivAndCiphertext);
    } catch (NoSuchAlgorithmException
        | NoSuchProviderException
        | NoSuchPaddingException
        | InvalidKeyException
        | InvalidAlgorithmParameterException
        | IllegalBlockSizeException
        | BadPaddingException e) {
      throw new IllegalArgumentException(
          "Invalid security configuration/exchange whilst sending message: " + e.getMessage(), e);
    }
  }

  /**
   * Receives an encrypted message from the remote handler, decrypts it using the shared secret key,
   * and returns the decrypted message.
   *
   * <p>This method first receives the encrypted message and the initialization vector (IV) from the
   * remote handler. It then extracts the ciphertext from the received data, initializes an AES-CBC
   * cipher with the shared secret key and the IV, and decrypts the ciphertext. Finally, it returns
   * the decrypted message as a String.
   *
   * @return the decrypted message
   * @throws IOException if there is an error receiving the message from the remote handler
   * @throws IllegalArgumentException if there is an error with the security configuration or
   *     exchange
   */
  @Override
  public String receiveMessage() throws IOException {
    checkInitialized();
    try {
      var ivAndCiphertext = handler.receiveBytes();
      var iv = new byte[12];
      System.arraycopy(ivAndCiphertext, 0, iv, 0, iv.length);
      var ivParameterSpec = new GCMParameterSpec(128, iv);

      // Extract encrypted part
      var ciphertextSize = ivAndCiphertext.length - iv.length;
      var ciphertext = new byte[ciphertextSize];
      System.arraycopy(ivAndCiphertext, iv.length, ciphertext, 0, ciphertextSize);

      var cipher = newCipherInstance();
      var aesKey = new SecretKeySpec(sharedKey.getEncoded(), 0, 32, "AES");
      cipher.init(Cipher.DECRYPT_MODE, aesKey, ivParameterSpec);
      var decryptedText = cipher.doFinal(ciphertext);
      return new String(decryptedText);
    } catch (NoSuchAlgorithmException
        | NoSuchProviderException
        | NoSuchPaddingException
        | InvalidKeyException
        | InvalidAlgorithmParameterException
        | IllegalBlockSizeException
        | BadPaddingException e) {
      throw new IllegalArgumentException(
          "Invalid security configuration/exchange whilst receiving message: " + e.getMessage(), e);
    }
  }

  @Override
  public void close() throws Exception {
    handler.close();
  }

  private Cipher newCipherInstance()
      throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {
    return Cipher.getInstance("AES/GCM/NoPadding");
  }

  private void checkInitialized() {
    if (!initialized) {
      throw new IllegalStateException("SecureMessageHandler has not been initialized.");
    }
  }
}
