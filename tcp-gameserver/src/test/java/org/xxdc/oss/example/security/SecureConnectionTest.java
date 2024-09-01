package org.xxdc.oss.example.security;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KEM;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import org.bouncycastle.pqc.jcajce.spec.KyberParameterSpec;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Secure KEM Connection using BouncyCastle Provider and Kyber for shared key generation and
 * exchange and AES for symmetric encryption of messages with a random IV per message.
 *
 * <p>Per: https://openjdk.org/jeps/452:
 *
 * <p>// Receiver side KeyPairGenerator g = KeyPairGenerator.getInstance("ABC"); KeyPair kp =
 * g.generateKeyPair(); publishKey(kp.getPublic());
 *
 * <p>// Sender side KEM kemS = KEM.getInstance("ABC-KEM"); PublicKey pkR = retrieveKey();
 * ABCKEMParameterSpec specS = new ABCKEMParameterSpec(...); KEM.Encapsulator e =
 * kemS.newEncapsulator(pkR, specS, null); KEM.Encapsulated enc = e.encapsulate(); SecretKey secS =
 * enc.key(); sendBytes(enc.encapsulation()); sendBytes(enc.params());
 *
 * <p>// Receiver side byte[] em = receiveBytes(); byte[] params = receiveBytes(); KEM kemR =
 * KEM.getInstance("ABC-KEM"); AlgorithmParameters algParams =
 * AlgorithmParameters.getInstance("ABC-KEM"); algParams.init(params); ABCKEMParameterSpec specR =
 * algParams.getParameterSpec(ABCKEMParameterSpec.class); KEM.Decapsulator d =
 * kemR.newDecapsulator(kp.getPrivate(), specR); SecretKey secR = d.decapsulate(em);
 *
 * <p>// secS and secR will be identical
 *
 * <p>1. Key Generation: Generate KEM key pairs on both client and server. 2. Key Encapsulation:
 * Client encapsulates a symmetric key with server’s public key and sends it. 3. Key Decapsulation:
 * Server decapsulates the symmetric key with its private key. 4. Secure Communication: Use the
 * symmetric key for encrypting and decrypting data over the TCP connection.
 *
 * @author Brian Corbin
 */
@SuppressWarnings("unused")
public class SecureConnectionTest {

  private static final Logger log = System.getLogger(SecureConnectionTest.class.getName());

  @Test
  public void testSecureConnection() {
    ExecutorService service = Executors.newVirtualThreadPerTaskExecutor();
    var serverTask =
        new Runnable() {
          public void run() {
            try (var serverSocket = new ServerSocket(9090)) {
              log.log(Level.INFO, "Server accepting connections on {0}...", serverSocket);
              var serverSession = service.submit(new Server(serverSocket));
              serverSession.get();
            } catch (IOException e) {
              e.printStackTrace();
            } catch (InterruptedException e) {
              e.printStackTrace();
            } catch (ExecutionException e) {
              throw new RuntimeException(e);
            }
          }
        };
    var clientTask =
        new Runnable() {
          public void run() {
            try (var clientSocket = new Socket("localhost", 9090)) {
              log.log(Level.INFO, "Client accepting connections on {0}...", clientSocket);
              var clientSession = service.submit(new Client(clientSocket));
              clientSession.get();
            } catch (Exception e) {
              Assert.fail("Exception during secure connection check.", e);
              log.log(Level.ERROR, e.getMessage(), e);
              throw new RuntimeException(e);
            }
          }
        };

    try {
      var serverSesion = service.submit(serverTask);
      var clientSession = service.submit(clientTask);

      serverSesion.get();
      clientSession.get();
      service.shutdown();
      service.awaitTermination(10, TimeUnit.MINUTES);
    } catch (Exception e) {
      Assert.fail("Exception during secure connection check.", e);
    }
  }

  private static class Server extends Remote implements Runnable {

    static {
      // Register provider 'BC' for symmetric shared key encryption
      Security.addProvider(new BouncyCastleProvider());
      // Register provider 'BCQPC' (BouncyCastle Post-Quantum Security Provider)
      // for secure encapsulated key exchange
      Security.addProvider(new BouncyCastlePQCProvider());
      // Register provider 'BCQPC' (BouncyCastle Post-Quantum Security Provider)
      // Register custom provider to use JDK KEMSpi with 'BCQPC'
      Security.addProvider(new KyberKEMProvider());
    }

    public Server(ServerSocket serverSocket) throws UnknownHostException, IOException {
      Socket clientSocket = serverSocket.accept();
      this.out = new ObjectOutputStream(clientSocket.getOutputStream());
      this.in = new ObjectInputStream(clientSocket.getInputStream());
    }

    @Override
    public void run() {
      try {
        // Receiver side
        KeyPairGenerator g = KeyPairGenerator.getInstance("Kyber");
        KeyPair kp = g.generateKeyPair();
        publishKey(kp.getPublic());

        // Receiver side
        byte[] em = receiveBytes();
        byte[] params = receiveBytes();
        KEM kemR = KEM.getInstance("Kyber", "BCPQC.KEM");
        AlgorithmParameters algParams = AlgorithmParameters.getInstance("Kyber");
        algParams.init(params);
        KyberParameterSpec specR = algParams.getParameterSpec(KyberParameterSpec.class);
        KEM.Decapsulator d = kemR.newDecapsulator(kp.getPrivate(), specR);
        SecretKey secR = d.decapsulate(em);
        log.log(Level.DEBUG, "SERVER: RCVR Secret Key: {0}", secR);
        log.log(Level.DEBUG, Arrays.toString(secR.getEncoded()));
        sendMessage("Hi, I'm the SERVER!", secR);
        String decrypted = receiveMessage(secR);
        log.log(Level.INFO, "SERVER: RCVR Message: {0} Length: {1}", decrypted, decrypted.length());

      } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    private void publishKey(PublicKey key) throws IOException {
      log.log(
          Level.DEBUG,
          "SERVER: SNDR -> Public Key: " + key + " Length: " + key.getEncoded().length);
      out.writeObject(key);
      out.flush();
    }

    public static void main(String[] args) {
      ExecutorService service = Executors.newVirtualThreadPerTaskExecutor();
      try (ServerSocket serverSocket = new ServerSocket(9090)) {
        log.log(Level.DEBUG, "Accepting connections on " + serverSocket + "...");
        service.submit(new Server(serverSocket));
        service.shutdown();
        service.awaitTermination(10, TimeUnit.MINUTES);
      } catch (IOException e) {
        log.log(Level.ERROR, e.getMessage(), e);
      } catch (InterruptedException e) {
        log.log(Level.ERROR, e.getMessage(), e);
      }
    }
  }

  private static class Client extends Remote implements Runnable {

    static {
      // Register provider 'BC' for symmetric shared key encryption
      Security.addProvider(new BouncyCastleProvider());
      // Register provider 'BCQPC' (BouncyCastle Post-Quantum Security Provider)
      Security.addProvider(new BouncyCastlePQCProvider());
      // Reginster custom provider to use KEMSpi with 'BCQPC'
      Security.addProvider(new KyberKEMProvider());
    }

    public Client(Socket socket) throws UnknownHostException, IOException {
      this.in = new ObjectInputStream(socket.getInputStream());
      this.out = new ObjectOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
      try {
        // Sender side
        KEM kemS = KEM.getInstance("Kyber", "BCPQC.KEM");
        PublicKey pkR = retrieveKey();
        // https://github.com/bcgit/bc-java/blob/main/prov/src/test/java/org/bouncycastle/pqc/jcajce/provider/test/KyberKeyPairGeneratorTest.java
        KyberParameterSpec specS = KyberParameterSpec.kyber1024;
        KEM.Encapsulator e = kemS.newEncapsulator(pkR, specS, null);
        KEM.Encapsulated enc = e.encapsulate();
        SecretKey secS = enc.key();
        log.log(Level.DEBUG, "CLIENT: NOOP Secret Key: {0}", secS);
        log.log(Level.DEBUG, () -> Arrays.toString(secS.getEncoded()));
        sendBytes(enc.encapsulation());
        log.log(
            Level.DEBUG,
            "CLIENT: SNDR Secret Key (Encap): {0} Length: {1}",
            enc.encapsulation(),
            enc.encapsulation().length);
        sendBytes(enc.params());
        log.log(
            Level.DEBUG,
            "CLIENT: SNDR Secret Key (Params): {0} Length: {1}",
            enc.params(),
            enc.params().length);
        sendMessage("Hi, I'm the CLIENT!", secS);
        String decrypted = receiveMessage(secS);
        log.log(Level.INFO, "CLIENT: RCVR Message: {0} Length: {1}", decrypted, decrypted.length());

      } catch (NoSuchAlgorithmException e) {
        log.log(Level.ERROR, e.getMessage(), e);
      } catch (ClassNotFoundException e) {
        log.log(Level.ERROR, e.getMessage(), e);
      } catch (IOException e) {
        log.log(Level.ERROR, e.getMessage(), e);
      } catch (InvalidKeyException e) {
        log.log(Level.ERROR, e.getMessage(), e);
      } catch (InvalidAlgorithmParameterException e) {
        log.log(Level.ERROR, e.getMessage(), e);
      } catch (NoSuchProviderException e) {
        log.log(Level.ERROR, e.getMessage(), e);
      } catch (NoSuchPaddingException e) {
        log.log(Level.ERROR, e.getMessage(), e);
      } catch (IllegalBlockSizeException e) {
        log.log(Level.ERROR, e.getMessage(), e);
      } catch (BadPaddingException e) {
        log.log(Level.ERROR, e.getMessage(), e);
      }
    }

    private PublicKey retrieveKey() throws ClassNotFoundException, IOException {
      PublicKey serverPublicKey = (PublicKey) in.readObject();
      log.log(
          Level.DEBUG,
          "CLIENT: RCVR Server Public Key: {0} Length: {1}",
          serverPublicKey,
          serverPublicKey.getEncoded().length);
      return serverPublicKey;
    }

    public static void main(String[] args) {
      ExecutorService service = Executors.newVirtualThreadPerTaskExecutor();
      try (Socket clientSocket = new Socket("localhost", 9090)) {
        log.log(Level.DEBUG, "Connecting to server on {0}...", clientSocket);
        service.submit(new Client(clientSocket));
        service.shutdown();
        service.awaitTermination(10, TimeUnit.MINUTES);
      } catch (IOException e) {
        log.log(Level.ERROR, e.getMessage(), e);
      } catch (InterruptedException e) {
        log.log(Level.ERROR, e.getMessage(), e);
      }
    }
  }

  private abstract static class Remote {

    protected ObjectOutputStream out;

    protected ObjectInputStream in;

    void sendBytes(byte[] bytes) throws IOException {
      out.writeInt(bytes.length);
      out.write(bytes);
      out.flush();
    }

    byte[] receiveBytes() throws IOException, ClassNotFoundException {
      int sz = in.readInt();
      log.log(Level.DEBUG, "Reading: {0} bytes.", sz);
      byte[] data = new byte[sz];
      in.readFully(data);
      return data;
    }

    void sendMessage(String message, SecretKey secretKey)
        throws NoSuchAlgorithmException,
            NoSuchProviderException,
            NoSuchPaddingException,
            InvalidKeyException,
            IllegalBlockSizeException,
            BadPaddingException,
            IOException,
            InvalidAlgorithmParameterException {
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding", "BC");
      byte[] iv = new byte[16];
      SecureRandom random = new SecureRandom();
      random.nextBytes(iv);
      IvParameterSpec ivSpec = new IvParameterSpec(iv);

      cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
      byte[] ciphertext = cipher.doFinal(message.getBytes());
      byte[] ivAndCiphertext = new byte[iv.length + ciphertext.length];
      System.arraycopy(iv, 0, ivAndCiphertext, 0, iv.length);
      System.arraycopy(ciphertext, 0, ivAndCiphertext, iv.length, ciphertext.length);

      sendBytes(ivAndCiphertext);
    }

    String receiveMessage(SecretKey secretKey)
        throws InvalidKeyException,
            InvalidAlgorithmParameterException,
            NoSuchAlgorithmException,
            NoSuchProviderException,
            NoSuchPaddingException,
            IllegalBlockSizeException,
            BadPaddingException,
            ClassNotFoundException,
            IOException {
      byte[] ivAndCiphertext = receiveBytes();
      byte[] iv = new byte[16];
      System.arraycopy(ivAndCiphertext, 0, iv, 0, iv.length);
      IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

      // Extract encrypted part
      int ciphertestSize = ivAndCiphertext.length - iv.length;
      byte[] ciphertext = new byte[ciphertestSize];
      System.arraycopy(ivAndCiphertext, iv.length, ciphertext, 0, ciphertestSize);

      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding", "BC");
      cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
      byte[] decryptedText = cipher.doFinal(ciphertext);
      String decrypted = new String(decryptedText);
      return decrypted;
    }
  }
}
