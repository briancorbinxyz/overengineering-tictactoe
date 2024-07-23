package org.example;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.DecapsulateException;
import javax.crypto.KEM.Encapsulated;
import javax.crypto.KEMSpi;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.bouncycastle.jcajce.SecretKeyWithEncapsulation;
import org.bouncycastle.jcajce.spec.KEMExtractSpec;
import org.bouncycastle.jcajce.spec.KEMGenerateSpec;
import org.bouncycastle.pqc.crypto.crystals.kyber.KyberParameters;
import org.bouncycastle.pqc.jcajce.spec.KyberParameterSpec;

/**
 * 
 */
public class KyberKEM implements KEMSpi {

    @Override
    public EncapsulatorSpi engineNewEncapsulator(PublicKey publicKey, AlgorithmParameterSpec spec,
            SecureRandom secureRandom) throws InvalidAlgorithmParameterException, InvalidKeyException {
        if (publicKey == null) {
            throw new InvalidKeyException("input key is null");
        }
        return switch(spec) {
            case KyberParameterSpec kSpec -> new KyberEncapsulatorDecapsulatorSpi(publicKey, kSpec);
            default -> throw new InvalidAlgorithmParameterException("Incompatible spec " + spec.getClass() + " for Kyber.");
        };
    }

    @Override
    public DecapsulatorSpi engineNewDecapsulator(PrivateKey privateKey, AlgorithmParameterSpec spec)
            throws InvalidAlgorithmParameterException, InvalidKeyException {
        if (privateKey == null) {
            throw new InvalidKeyException("input key is null");
        }
        return switch(spec) {
            case KyberParameterSpec kSpec -> new KyberEncapsulatorDecapsulatorSpi(privateKey, kSpec);
            default -> throw new InvalidAlgorithmParameterException("Incompatible spec " + spec.getClass() + " for Kyber.");
        };
    }

    private static class KyberEncapsulatorDecapsulatorSpi implements EncapsulatorSpi, DecapsulatorSpi {

        private PublicKey publicKey;

        private PrivateKey privateKey;

        private KyberParameterSpec parameterSpec; 

        private static Map<KyberParameterSpec, KyberParameters> paramsBySpec;

        static {
            paramsBySpec = new HashMap<>(3);
            paramsBySpec.put(KyberParameterSpec.kyber512, KyberParameters.kyber512);
            paramsBySpec.put(KyberParameterSpec.kyber768, KyberParameters.kyber768);
            paramsBySpec.put(KyberParameterSpec.kyber1024, KyberParameters.kyber1024);
        }

        public KyberEncapsulatorDecapsulatorSpi(PublicKey publicKey, KyberParameterSpec parameterSpec) {
            this.publicKey = publicKey;
            this.parameterSpec = parameterSpec;
        }

        public KyberEncapsulatorDecapsulatorSpi(PrivateKey privateKey, KyberParameterSpec parameterSpec) {
            this.privateKey = privateKey;
            this.parameterSpec = parameterSpec;
        }

        @Override
        public Encapsulated engineEncapsulate(int from, int to, String algorithm) {
            try {
                KeyGenerator keyGenerator = KeyGenerator.getInstance("Kyber", "BCPQC");
                KEMGenerateSpec kemGenerateSpec = new KEMGenerateSpec(publicKey, parameterSpec.getName());
                keyGenerator.init(kemGenerateSpec);
                SecretKeyWithEncapsulation key = (SecretKeyWithEncapsulation) keyGenerator.generateKey(); 
                return new Encapsulated(key, key.getEncapsulation(), new byte[]{});
            } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
                throw new UnsupportedOperationException(e); 
            }
        }

        @Override
        public SecretKey engineDecapsulate(byte[] encapsulation, int from, int to, String algorithm)
                throws DecapsulateException {
            try {
                KeyGenerator keyGenerator = KeyGenerator.getInstance("Kyber", "BCPQC");
                KEMExtractSpec kemExtractSpec = new KEMExtractSpec(privateKey, encapsulation, parameterSpec.getName());
                keyGenerator.init(kemExtractSpec);
                return keyGenerator.generateKey();
            } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
                throw new DecapsulateException("Failed whilst decapsulating.", e); 
            }
        }

        @Override
        public int engineSecretSize() {
            return paramsBySpec.get(parameterSpec).getSessionKeySize();
        }

        @Override
        public int engineEncapsulationSize() {
            return paramsBySpec.get(parameterSpec).getSessionKeySize();
        }


    }

}
