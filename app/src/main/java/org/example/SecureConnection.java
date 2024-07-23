package org.example;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.crypto.KEM;
import javax.crypto.KEMSpi;
import javax.crypto.KEMSpi.EncapsulatorSpi;
import javax.crypto.SecretKey;

import org.bouncycastle.jcajce.spec.KEMGenerateSpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import org.bouncycastle.pqc.jcajce.spec.KyberParameterSpec;

/**
 * Secure KEM Connection using BouncyCastle Provider and Kyber
 * 
 * Per: https://openjdk.org/jeps/452
 * 
 * // Receiver side
 * KeyPairGenerator g = KeyPairGenerator.getInstance("ABC");
 * KeyPair kp = g.generateKeyPair();
 * publishKey(kp.getPublic());
 * 
 * // Sender side
 * KEM kemS = KEM.getInstance("ABC-KEM");
 * PublicKey pkR = retrieveKey();
 * ABCKEMParameterSpec specS = new ABCKEMParameterSpec(...);
 * KEM.Encapsulator e = kemS.newEncapsulator(pkR, specS, null);
 * KEM.Encapsulated enc = e.encapsulate();
 * SecretKey secS = enc.key();
 * sendBytes(enc.encapsulation());
 * sendBytes(enc.params());
 * 
 * // Receiver side
 * byte[] em = receiveBytes();
 * byte[] params = receiveBytes();
 * KEM kemR = KEM.getInstance("ABC-KEM");
 * AlgorithmParameters algParams = AlgorithmParameters.getInstance("ABC-KEM");
 * algParams.init(params);
 * ABCKEMParameterSpec specR = algParams.getParameterSpec(ABCKEMParameterSpec.class);
 * KEM.Decapsulator d = kemR.newDecapsulator(kp.getPrivate(), specR);
 * SecretKey secR = d.decapsulate(em);
 * 
 * // secS and secR will be identical
 * 
 * 1. Key Generation: Generate KEM key pairs on both client and server.
 * 2. Key Encapsulation: Client encapsulates a symmetric key with server’s public key and sends it.
 * 3. Key Decapsulation: Server decapsulates the symmetric key with its private key.
 * 4. Secure Communication: Use the symmetric key for encrypting and decrypting data over the TCP connection.
 */
public class SecureConnection {

    private static class Server implements Runnable {

        static {
            // Register provider 'BCQPC' (BouncyCastle Post-Quantum Security Provider)
            Security.addProvider(new BouncyCastlePQCProvider());
        }

        private final Socket clientSocket;

        private final ObjectOutputStream out;

        private final ObjectInputStream in;

        public Server(ServerSocket serverSocket) throws UnknownHostException, IOException {
            this.clientSocket = serverSocket.accept();
            this.out = new ObjectOutputStream(clientSocket.getOutputStream());
            this.in = new ObjectInputStream(clientSocket.getInputStream());
        }

        @Override
        public void run() {
            try {
                KeyPairGenerator g = KeyPairGenerator.getInstance("Kyber");
                KeyPair kp = g.generateKeyPair(); 
                publishKey(kp.getPublic());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void publishKey(PublicKey key) throws IOException {
            System.out.println("Server: Public Key: " + key);
            out.writeObject(key);
        }
        public static void main(String [] args) {
            ExecutorService service = Executors.newVirtualThreadPerTaskExecutor();
            try (ServerSocket serverSocket = new ServerSocket(9090)) {
                System.out.println("Accepting connections on " + serverSocket + "...");
                service.submit(new Server(serverSocket));
                service.shutdown();
                service.awaitTermination(10, TimeUnit.MINUTES);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private static class Client implements Runnable {

        static {
            // Register provider 'BCQPC' (BouncyCastle Post-Quantum Security Provider)
            Security.addProvider(new BouncyCastlePQCProvider());
            Security.addProvider(new KyberKEMProvider());
        }

        private final ObjectOutputStream out;

        private final ObjectInputStream in;

        public Client(Socket socket) throws UnknownHostException, IOException {
            this.in = new ObjectInputStream(socket.getInputStream());
            this.out = new ObjectOutputStream(socket.getOutputStream());
        }

        @Override
        public void run() {
            try {
                KEM kemS = KEM.getInstance("Kyber", "BCPQC.KEM");
                KEMSpi spi = new KyberKEM();
                PublicKey pkR = retrieveKey();
                // https://github.com/bcgit/bc-java/blob/main/prov/src/test/java/org/bouncycastle/pqc/jcajce/provider/test/KyberKeyPairGeneratorTest.java
                KyberParameterSpec specS = KyberParameterSpec.kyber1024;
                KEM.Encapsulator e = kemS.newEncapsulator(pkR, specS, null);
                KEM.Encapsulated enc = e.encapsulate();
                SecretKey secS = enc.key();
                sendBytes(enc.encapsulation());
                sendBytes(enc.params());
                throw new UnsupportedOperationException("Unimplemented method 'run'");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            } catch (NoSuchProviderException e) {
                e.printStackTrace();
            }
        }

        private void sendBytes(byte[] encapsulation) throws IOException {
            out.write(encapsulation);
        }

        private PublicKey retrieveKey() throws ClassNotFoundException, IOException {
            PublicKey serverPublicKey = (PublicKey) in.readObject();
            System.out.println("Client: Server Public Key: " + serverPublicKey);
            return serverPublicKey;
        }

        public static void main(String [] args) {
            ExecutorService service = Executors.newVirtualThreadPerTaskExecutor();
            try (Socket clientSocket = new Socket("localhost", 9090)) {
                System.out.println("Connecting to server on " + clientSocket + "...");
                service.submit(new Client(clientSocket));
                service.shutdown();
                service.awaitTermination(10, TimeUnit.MINUTES);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}
