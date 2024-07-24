package org.example;

import java.security.spec.AlgorithmParameterSpec;
import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.pqc.crypto.crystals.kyber.KyberParameters;
import org.bouncycastle.pqc.jcajce.spec.KyberParameterSpec;

/**
 * Unify the bouncy castle kyber parameters, spec and JDK {@code AlgorithmParameterSpec}
 */
public enum KyberConfigs {
    Kyber512(KyberParameters.kyber512),
    Kyber768(KyberParameters.kyber768),
    Kyber1024(KyberParameters.kyber1024);

    private static Map<KyberParameterSpec, KyberConfigs> paramsBySpec;

    private static Map<Class<? extends KyberParameterSpec>, KyberParameterSpec> specsBySpecClass;

    private final KyberParameters params;

    static {
        paramsBySpec = new HashMap<>(3);
        paramsBySpec.put(KyberParameterSpec.kyber512, Kyber512);
        paramsBySpec.put(KyberParameterSpec.kyber768, Kyber768);
        paramsBySpec.put(KyberParameterSpec.kyber1024, Kyber1024);

        specsBySpecClass = new HashMap<>(3);
        specsBySpecClass.put(KyberParameterSpec.kyber512.getClass(), KyberParameterSpec.kyber512);
        specsBySpecClass.put(KyberParameterSpec.kyber768.getClass(), KyberParameterSpec.kyber768);
        specsBySpecClass.put(KyberParameterSpec.kyber1024.getClass(), KyberParameterSpec.kyber1024);
    }

    public static KyberConfigs byKyberParameterSpec(KyberParameterSpec spec) {
        return paramsBySpec.get(spec);
    }

    public static <T extends AlgorithmParameterSpec> T newParameterSpec(Class<T> spec) {
        return spec.cast(specsBySpecClass.get(spec));
    }

    KyberConfigs(KyberParameters params) {
        this.params = params;
    }

    public KyberParameters parameters() {
        return this.params;
    }

    public static KyberConfigs fromEncoded(byte[] encodedParams) {
        return valueOf(new String(encodedParams));
    }

    public byte[] encode() {
        return name().getBytes();
    }

}
