package org.example;

import java.io.IOException;
import java.security.AlgorithmParametersSpi;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;

import org.bouncycastle.pqc.jcajce.spec.KyberParameterSpec;

public class KyberParametersSpi extends AlgorithmParametersSpi {

    private KyberConfigs paramEngine;

    @Override
    protected void engineInit(AlgorithmParameterSpec paramSpec) throws InvalidParameterSpecException {
        this.paramEngine = switch(paramSpec) {
            case KyberParameterSpec p -> KyberConfigs.byKyberParameterSpec(p);
            default -> throw new InvalidParameterSpecException(paramSpec + " is not a supported paramtere spec.");
        };
    }

    @Override
    protected void engineInit(byte[] params) throws IOException {
        this.paramEngine = KyberConfigs.fromEncoded(params);
    }

    @Override
    protected void engineInit(byte[] params, String format) throws IOException {
        this.paramEngine = KyberConfigs.fromEncoded(params);
    }

    @Override
    protected <T extends AlgorithmParameterSpec> T engineGetParameterSpec(Class<T> paramSpec)
            throws InvalidParameterSpecException {
        if (!KyberParameterSpec.class.isAssignableFrom(paramSpec)) {
            throw new InvalidParameterSpecException(paramSpec + " is not a supporeted parameter spec.");
        }
        return KyberConfigs.newParameterSpec(paramSpec);
    }

    @Override
    protected byte[] engineGetEncoded() throws IOException {
        return paramEngine.encode();
    }

    @Override
    protected byte[] engineGetEncoded(String format) throws IOException {
        return paramEngine.encode();
    }

    @Override
    protected String engineToString() {
        return paramEngine.toString();
    }

}
