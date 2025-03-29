package org.xxdc.oss.example.security;

import static org.testng.Assert.assertNotNull;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.NamedParameterSpec;
import javax.crypto.KEM;
import org.testng.annotations.Test;

public class MLKEMSpiTest {

  @Test
  public void test_can_generate_a_keypair() throws NoSuchAlgorithmException {
    // https://openjdk.org/jeps/496
    KeyPairGenerator g = KeyPairGenerator.getInstance("ML-KEM");
    KeyPair kp = g.generateKeyPair();
    assertNotNull(kp);
  }

  @Test
  public void test_can_generate_a_1024bit_keypair() throws NoSuchAlgorithmException {
    // https://openjdk.org/jeps/496
    KeyPairGenerator g = KeyPairGenerator.getInstance("ML-KEM-1024");
    KeyPair kp = g.generateKeyPair();
    assertNotNull(kp);
  }

  @Test
  public void test_can_generate_a_1024bit_keypair_using_namedparameterspec()
      throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
    // https://openjdk.org/jeps/496
    KeyPairGenerator g = KeyPairGenerator.getInstance("ML-KEM");
    g.initialize(NamedParameterSpec.ML_KEM_1024);
    KeyPair kp = g.generateKeyPair();
    assertNotNull(kp);
  }

  @Test
  public void test_can_instantiate_the_kem() throws NoSuchAlgorithmException {
    KEM kem = KEM.getInstance("ML-KEM");
    assertNotNull(kem);
  }
}
