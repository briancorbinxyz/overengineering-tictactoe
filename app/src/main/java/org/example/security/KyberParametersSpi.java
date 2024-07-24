package org.example.security;

import java.io.IOException;
import java.security.AlgorithmParametersSpi;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;

import org.bouncycastle.pqc.jcajce.spec.KyberParameterSpec;

/**
 * An implementation of the {@link AlgorithmParametersSpi} interface for Kyber parameters.
 * This class is responsible for handling the initialization and retrieval of Kyber algorithm parameters.

 * @author Brian Corbin
 */
public class KyberParametersSpi extends AlgorithmParametersSpi {

    private KyberParams params;

    @Override
    protected void engineInit(AlgorithmParameterSpec paramSpec) throws InvalidParameterSpecException {
        this.params = switch(paramSpec) {
            case KyberParameterSpec p -> KyberParams.byKyberParameterSpec(p);
            default -> throw new InvalidParameterSpecException(paramSpec + " is not a supported paramtere spec.");
        };
    }

    @Override
    protected void engineInit(byte[] params) throws IOException {
        this.params = KyberParams.fromEncoded(params);
    }

    @Override
    protected void engineInit(byte[] params, String format) throws IOException {
        this.params = KyberParams.fromEncoded(params);
    }

    @Override
    protected <T extends AlgorithmParameterSpec> T engineGetParameterSpec(Class<T> paramSpec)
            throws InvalidParameterSpecException {
        if (!KyberParameterSpec.class.isAssignableFrom(paramSpec)) {
            throw new InvalidParameterSpecException(paramSpec + " is not a supporeted parameter spec.");
        }
        return KyberParams.newParameterSpec(paramSpec);
    }

    @Override
    protected byte[] engineGetEncoded() throws IOException {
        return params.encode();
    }

    @Override
    protected byte[] engineGetEncoded(String format) throws IOException {
        return params.encode();
    }

    @Override
    protected String engineToString() {
        return params.toString();
    }

}
