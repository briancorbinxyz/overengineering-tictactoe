package org.xxdc.oss.example.security;

import java.security.spec.AlgorithmParameterSpec;
import java.util.HashMap;
import java.util.Map;
import org.bouncycastle.pqc.crypto.crystals.kyber.KyberParameters;
import org.bouncycastle.pqc.jcajce.spec.KyberParameterSpec;

/**
 * Unify the bouncy castle kyber parameters, spec and JDK {@code AlgorithmParameterSpec}
 *
 * @author Brian Corbin
 */
public enum KyberParams {
  /** Kyber-512 */
  Kyber512(KyberParameters.kyber512),
  /** Kyber-768 */
  Kyber768(KyberParameters.kyber768),
  /** Kyber-1024 */
  Kyber1024(KyberParameters.kyber1024);

  private static Map<KyberParameterSpec, KyberParams> paramsBySpec;

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

  /**
   * Returns the {@link KyberParams} instance corresponding to the given {@link KyberParameterSpec}.
   *
   * @param spec the {@link KyberParameterSpec} to get the {@link KyberParams} for
   * @return the {@link KyberParams} instance corresponding to the given {@link KyberParameterSpec}
   */
  public static KyberParams byKyberParameterSpec(KyberParameterSpec spec) {
    return paramsBySpec.get(spec);
  }

  /**
   * Returns a new {@link AlgorithmParameterSpec} instance of the specified class, based on the
   * corresponding {@link KyberParameterSpec} in the {@link #specsBySpecClass} map.
   *
   * @param <T> the type of {@link AlgorithmParameterSpec} to return
   * @param spec the class of {@link AlgorithmParameterSpec} to return
   * @return a new instance of the specified {@link AlgorithmParameterSpec} class
   */
  public static <T extends AlgorithmParameterSpec> T newParameterSpec(Class<T> spec) {
    return spec.cast(specsBySpecClass.get(spec));
  }

  KyberParams(KyberParameters params) {
    this.params = params;
  }

  /**
   * Returns the {@link KyberParameters} instance associated with this {@link KyberParams} enum
   * value.
   *
   * @return the {@link KyberParameters} instance associated with this {@link KyberParams} enum
   *     value
   */
  public KyberParameters parameters() {
    return this.params;
  }

  /**
   * Returns the {@link KyberParams} instance corresponding to the provided encoded parameter bytes.
   *
   * @param encodedParams the encoded bytes representing a {@link KyberParams} instance
   * @return the {@link KyberParams} instance corresponding to the provided encoded parameter bytes
   */
  public static KyberParams fromEncoded(byte[] encodedParams) {
    return valueOf(new String(encodedParams));
  }

  /**
   * Encodes this {@link KyberParams} instance as a byte array.
   *
   * @return a byte array representation of this {@link KyberParams} instance
   */
  public byte[] encode() {
    return name().getBytes();
  }
}
