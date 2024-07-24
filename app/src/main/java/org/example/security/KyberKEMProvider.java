package org.example.security;

import java.security.Provider;

/**
 * A provider for the KyberKEM key encapsulation mechanism.
 * This provider registers the "Kyber" KEM and "Kyber" algorithm parameters.

 * @author Brian Corbin
 */
public class KyberKEMProvider extends Provider {

    public KyberKEMProvider() {
        super("BCPQC.KEM", "1.0", "Provider for KyberKEM");
        put("KEM.Kyber", KyberKEMSpi.class.getName());
        put("AlgorithmParameters.Kyber", KyberParametersSpi.class.getName());
    }
}
