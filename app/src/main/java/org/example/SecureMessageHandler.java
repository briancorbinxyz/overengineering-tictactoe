package org.example;

import java.io.IOException;

import java.security.Security;
import java.security.spec.InvalidParameterSpecException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import org.example.security.KyberKEMProvider;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

public abstract class SecureMessageHandler implements MessageHandler {

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

	@Override
	public abstract void init() throws IOException;

	@Override
	public void sendMessage(String message) throws IOException {
	}

	@Override
	public String receiveMessage() throws IOException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'receiveMessage'");
	}

	public static class SecureServerMessageHandler extends SecureMessageHandler {

		public SecureServerMessageHandler(RemoteMessageHandler handler) {
			super(handler);
		}

		@Override
		public void init() throws IOException {
			try {
				var g = KeyPairGenerator.getInstance("Kyber");
				var kp = g.generateKeyPair(); 
				publishKey(kp.getPublic());

				// Receiver side
				byte[] em = handler.receiveBytes();
				byte[] params = handler.receiveBytes();
				KEM kemR = KEM.getInstance("Kyber", "BCPQC.KEM");
				AlgorithmParameters algParams = AlgorithmParameters.getInstance("Kyber");
				algParams.init(params);
				KyberParameterSpec specR = algParams.getParameterSpec(KyberParameterSpec.class);
				KEM.Decapsulator d = kemR.newDecapsulator(kp.getPrivate(), specR);
				sharedKey = d.decapsulate(em);
				handler.init();
				initialized = true;				
			} catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidParameterSpecException | InvalidKeyException | InvalidAlgorithmParameterException | DecapsulateException e) {
				throw new IllegalArgumentException("Invalid security configuration/exchange: " + e.getMessage(), e);
			} finally {

			}
		}

		public void publishKey(PublicKey pk) throws IOException {
			handler.sendObject(pk);
		}

	}

	public static class SecureClientMessageHandler extends SecureMessageHandler {

		public SecureClientMessageHandler(RemoteMessageHandler handler) {
			super(handler);
		}
	}

}
