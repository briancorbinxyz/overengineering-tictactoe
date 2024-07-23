package org.example;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Secure KEM Connection
 * 
 * 1. Key Generation: Generate KEM key pairs on both client and server.
 * 2. Key Encapsulation: Client encapsulates a symmetric key with server’s public key and sends it.
 * 3. Key Decapsulation: Server decapsulates the symmetric key with its private key.
 * 4. Secure Communication: Use the symmetric key for encrypting and decrypting data over the TCP connection.
 */
public class SecureConnection {

    private static class Client implements Runnable {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'run'");
        }

    }

    private static class Server implements Runnable {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'run'");
        }

    }

    public static void main() {
        ExecutorService service = Executors.newVirtualThreadPerTaskExecutor();

    }
}
