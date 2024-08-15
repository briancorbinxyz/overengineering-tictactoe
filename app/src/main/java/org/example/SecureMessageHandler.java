package org.example;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
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
import java.security.spec.InvalidParameterSpecException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.DecapsulateException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KEM;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import org.bouncycastle.pqc.jcajce.spec.KyberParameterSpec;
import org.example.security.KyberKEMProvider;

public abstract sealed class SecureMessageHandler implements MessageHandler {

    private static final Logger log = System.getLogger(SecureMessageHandler.class.getName());

    protected final RemoteMessageHandler handler;

    protected SecretKey sharedKey;

    protected boolean initialized = false;

    public SecureMessageHandler(RemoteMessageHandler handler) {
        this.handler = handler;
        registerSecurityProviders();
    }

    private void registerSecurityProviders() {
        Security.addProvider(new BouncyCastleProvider());
        Security.addProvider(new BouncyCastlePQCProvider());
        Security.addProvider(new KyberKEMProvider());
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
     * Exchange a shared secret key with remote connection using the Kyber Key Encapsulation
     * Mechanism (KEM).
     *
     * <p>This method generates a new Kyber key pair, publishes the public key to the remote
     * handler, and then receives an encapsulated shared secret from the remote handler. It uses the
     * private key to decapsulate the shared secret.
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
            var iv = new byte[16];
            var random = new SecureRandom();
            random.nextBytes(iv);
            var ivSpec = new IvParameterSpec(iv);

            cipher.init(Cipher.ENCRYPT_MODE, sharedKey, ivSpec);
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
                    "Invalid security configuration/exchange whilst sending message: "
                            + e.getMessage(),
                    e);
        }
    }

    /**
     * Receives an encrypted message from the remote handler, decrypts it using the shared secret
     * key, and returns the decrypted message.
     *
     * <p>This method first receives the encrypted message and the initialization vector (IV) from
     * the remote handler. It then extracts the ciphertext from the received data, initializes an
     * AES-CBC cipher with the shared secret key and the IV, and decrypts the ciphertext. Finally,
     * it returns the decrypted message as a String.
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
            var iv = new byte[16];
            System.arraycopy(ivAndCiphertext, 0, iv, 0, iv.length);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

            // Extract encrypted part
            var ciphertextSize = ivAndCiphertext.length - iv.length;
            var ciphertext = new byte[ciphertextSize];
            System.arraycopy(ivAndCiphertext, iv.length, ciphertext, 0, ciphertextSize);

            var cipher = newCipherInstance();
            cipher.init(Cipher.DECRYPT_MODE, sharedKey, ivParameterSpec);
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
                    "Invalid security configuration/exchange whilst receiving message: "
                            + e.getMessage(),
                    e);
        }
    }

    @Override
    public void close() throws Exception {
        handler.close();
    }

    private Cipher newCipherInstance()
            throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {
        return Cipher.getInstance("AES/CBC/PKCS5Padding", "BC");
    }

    private void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException("SecureMessageHandler has not been initialized.");
        }
    }

    /**
     * Represents a secure message handler for the server side of a secure communication channel.
     * This class extends the `SecureMessageHandler` class and is responsible for initializing the
     * secure channel, exchanging the shared secret key with the client, and providing methods for
     * sending and receiving encrypted messages.
     */
    public static final class Server extends SecureMessageHandler {
        /**
         * Constructs a new `SecureServerMessageHandler` instance with the given
         * `RemoteMessageHandler`.
         *
         * @param remoteMessageHandler the `RemoteMessageHandler` to use for sending and receiving
         *     messages
         */
        public Server(RemoteMessageHandler remoteMessageHandler) {
            super(remoteMessageHandler);
        }

        /**
         * Initializes the secure message handler by setting up the secure channel and exchanging
         * the shared secret key with the client.
         *
         * @throws IOException if there is an error during the initialization process
         */
        @Override
        public void init() throws IOException {
            try {
                handler.init();
                log.log(
                        Level.DEBUG,
                        "Initializing secure channel for {0}. Exchanging shared key.",
                        getClass().getSimpleName());
                sharedKey = exchangeSharedKey();
                initialized = true;
                log.log(
                        Level.DEBUG,
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
         * Exchanges the shared secret key with the client using the Kyber key encapsulation
         * mechanism (KEM).
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
            var keyPair = keyPairGen.generateKeyPair();
            return keyPair;
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

    /**
     * Represents a secure message handler for the client side of a secure communication channel.
     * This class extends the `SecureMessageHandler` class and is responsible for initializing the
     * secure channel, exchanging the shared secret key with the server, and providing methods for
     * sending and receiving encrypted messages.
     */
    public static final class Client extends SecureMessageHandler {

        /**
         * Constructs a new `SecureClientMessageHandler` instance with the given
         * `RemoteMessageHandler`.
         *
         * @param handler the `RemoteMessageHandler` to use for the secure communication channel
         */
        public Client(RemoteMessageHandler handler) {
            super(handler);
        }

        /**
         * Initializes the secure message handler by setting up the secure channel and exchanging
         * the shared secret key with the client.
         *
         * @throws IOException if there is an error during the initialization process
         */
        @Override
        public void init() throws IOException {
            // Sender side
            try {
                handler.init();
                log.log(
                        Level.DEBUG,
                        "Initializing secure channel for {0}. Exchanging shared key.",
                        getClass().getSimpleName());
                sharedKey = exchangeSharedKey();
                initialized = true;
                log.log(
                        Level.DEBUG,
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
         * Initializes the secure message handler by exchanging a shared key with the remote party.
         * This method is called on the sender side to set up the secure communication channel.
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
         * @throws ClassNotFoundException if the class is not found whilst deserializing from the
         *     message handler
         * @throws IOException if there is an error with the communications channel
         */
        private PublicKey retrieveKey() throws ClassNotFoundException, IOException {
            return (PublicKey) handler.receiveObject();
        }
    }
}
