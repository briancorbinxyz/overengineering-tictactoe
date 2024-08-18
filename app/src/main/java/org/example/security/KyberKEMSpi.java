package org.example.security;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.DecapsulateException;
import javax.crypto.KEM.Encapsulated;
import javax.crypto.KEMSpi;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import org.bouncycastle.jcajce.SecretKeyWithEncapsulation;
import org.bouncycastle.jcajce.spec.KEMExtractSpec;
import org.bouncycastle.jcajce.spec.KEMGenerateSpec;
import org.bouncycastle.pqc.jcajce.spec.KyberParameterSpec;

/**
 * Implements the Kyber Key Encapsulation Mechanism (KEM) as a Java Cryptography Extension (JCE)
 * provider. The `KyberKEMSpi` class provides the implementation of the `KEMSpi` interface, which
 * allows for the creation of Kyber-based encapsulators and decapsulators.
 *
 * <p>The `KyberEncapsulatorDecapsulatorSpi` inner class implements the actual encapsulation and
 * decapsulation logic, using the Bouncy Castle provider for the underlying Kyber algorithm
 * implementation.
 *
 * @author Brian Corbin
 */
public class KyberKEMSpi implements KEMSpi {

  @Override
  public EncapsulatorSpi engineNewEncapsulator(
      PublicKey publicKey, AlgorithmParameterSpec spec, SecureRandom secureRandom)
      throws InvalidAlgorithmParameterException, InvalidKeyException {
    if (publicKey == null) {
      throw new InvalidKeyException("input key is null");
    }
    return switch (spec) {
      case KyberParameterSpec kSpec ->
          new KyberEncapsulatorDecapsulatorSpi(publicKey, kSpec, secureRandom);
      default ->
          throw new InvalidAlgorithmParameterException(
              "Incompatible spec " + spec.getClass() + " for Kyber.");
    };
  }

  @Override
  public DecapsulatorSpi engineNewDecapsulator(PrivateKey privateKey, AlgorithmParameterSpec spec)
      throws InvalidAlgorithmParameterException, InvalidKeyException {
    if (privateKey == null) {
      throw new InvalidKeyException("input key is null");
    }
    return switch (spec) {
      case KyberParameterSpec kSpec -> new KyberEncapsulatorDecapsulatorSpi(privateKey, kSpec);
      default ->
          throw new InvalidAlgorithmParameterException(
              "Incompatible spec " + spec.getClass() + " for Kyber.");
    };
  }

  private static class KyberEncapsulatorDecapsulatorSpi
      implements EncapsulatorSpi, DecapsulatorSpi {

    private PublicKey publicKey;

    private PrivateKey privateKey;

    private KyberParameterSpec parameterSpec;

    private SecureRandom random;

    public KyberEncapsulatorDecapsulatorSpi(
        PublicKey publicKey, KyberParameterSpec parameterSpec, SecureRandom random) {
      this.publicKey = publicKey;
      this.parameterSpec = parameterSpec;
      this.random = random;
    }

    public KyberEncapsulatorDecapsulatorSpi(
        PrivateKey privateKey, KyberParameterSpec parameterSpec) {
      this.privateKey = privateKey;
      this.parameterSpec = parameterSpec;
    }

    @Override
    public Encapsulated engineEncapsulate(int from, int to, String algorithm) {
      try {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("Kyber", "BCPQC");
        KEMGenerateSpec kemGenerateSpec = new KEMGenerateSpec(publicKey, parameterSpec.getName());
        if (random == null) {
          keyGenerator.init(kemGenerateSpec);
        } else {
          keyGenerator.init(kemGenerateSpec, random);
        }
        SecretKeyWithEncapsulation key = (SecretKeyWithEncapsulation) keyGenerator.generateKey();
        return new Encapsulated(
            key, key.getEncapsulation(), KyberParams.byKyberParameterSpec(parameterSpec).encode());
      } catch (NoSuchAlgorithmException
          | NoSuchProviderException
          | InvalidAlgorithmParameterException e) {
        throw new UnsupportedOperationException(e);
      }
    }

    @Override
    public SecretKey engineDecapsulate(byte[] encapsulation, int from, int to, String algorithm)
        throws DecapsulateException {
      try {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("Kyber", "BCPQC");
        KEMExtractSpec kemExtractSpec =
            new KEMExtractSpec(privateKey, encapsulation, parameterSpec.getName());
        keyGenerator.init(kemExtractSpec);
        return keyGenerator.generateKey();
      } catch (NoSuchAlgorithmException
          | NoSuchProviderException
          | InvalidAlgorithmParameterException e) {
        throw new DecapsulateException("Failed whilst decapsulating.", e);
      }
    }

    @Override
    public int engineSecretSize() {
      return KyberParams.byKyberParameterSpec(parameterSpec).parameters().getSessionKeySize();
    }

    @Override
    public int engineEncapsulationSize() {
      return KyberParams.byKyberParameterSpec(parameterSpec).parameters().getSessionKeySize();
    }
  }
}
