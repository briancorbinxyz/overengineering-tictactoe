package org.example;

import java.security.Provider;

public class KyberKEMProvider extends Provider {

    public KyberKEMProvider() {
        super("BCPQC.KEM", "1.0", "Provider for KyberKEM");
        put("KEM.Kyber", "org.example.KyberKEM");
    }
}
