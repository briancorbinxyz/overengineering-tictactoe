package org.example;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KEM;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;

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
@SuppressWarnings("unused")
public class SecureConnection {

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
            System.out.println("Reading: " + sz + " bytes.");
            byte[] data = new byte[sz];
            in.readFully(data);
            return data;
        }

        void sendMessage(String message, SecretKey secretKey) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException, InvalidAlgorithmParameterException {
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

        String receiveMessage(SecretKey secretKey) throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, ClassNotFoundException, IOException {
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
                System.out.println("SERVER: RCVR Secret Key: " + secR);
                System.out.println(Arrays.toString(secR.getEncoded()));
                sendMessage("Hi, I'm the SERVER!", secR);
                String decrypted = receiveMessage(secR);
                System.out.println("SERVER: RCVR Message: " + decrypted + " Length: " + decrypted.length());

                //
                in.read();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void publishKey(PublicKey key) throws IOException {
            System.out.println("SERVER: SNDR -> Public Key: " + key + " Length: " + key.getEncoded().length);
            out.writeObject(key);
            out.flush();
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
                System.out.println("CLIENT: NOOP Secret Key: " + secS);
                System.out.println(Arrays.toString(secS.getEncoded()));
                sendBytes(enc.encapsulation());
                System.out.println("CLIENT: SNDR Secret Key (Encap): " + enc.encapsulation() + " Length: " + enc.encapsulation().length);
                sendBytes(enc.params());
                System.out.println("CLIENT: SNDR Secret Key (Params): " + enc.params() + " Length: " + enc.params().length);

                sendMessage("Hi, I'm the CLIENT!", secS);
                String decrypted = receiveMessage(secS);
                System.out.println("CLIENT: RCVR Message: " + decrypted + " Length: " + decrypted.length());
                

                //
                in.read();
                
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
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            }
        }

        private PublicKey retrieveKey() throws ClassNotFoundException, IOException {
            PublicKey serverPublicKey = (PublicKey) in.readObject();
            System.out.println("CLIENT: RCVR Server Public Key: " + serverPublicKey + " Length: " + serverPublicKey.getEncoded().length);
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
