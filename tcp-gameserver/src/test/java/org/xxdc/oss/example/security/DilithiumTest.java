package org.xxdc.oss.example.security;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Test to explore the functionality of https://openjdk.org/jeps/497 */
public class DilithiumTest {

  @Test
  public void should_generate_dilithium_keypair() throws Exception {
    KeyPair kp = generateKeyPair();
    Assert.assertNotNull(kp);
  }

  @Test
  public void should_be_able_to_sign_and_verify_with_dilithium() throws Exception {
    // Generate
    KeyPair kp = generateKeyPair();

    // Sign
    byte[] msg = "AGI is fiction".getBytes();
    Signature ss = Signature.getInstance("ML-DSA");
    ss.initSign(kp.getPrivate());
    ss.update(msg);
    byte[] sig = ss.sign();

    // Verify
    Signature sv = Signature.getInstance("ML-DSA");
    sv.initVerify(kp.getPublic());
    sv.update(msg);
    Assert.assertTrue(sv.verify(sig));
  }

  private static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
    // https://nvlpubs.nist.gov/nistpubs/fips/nist.fips.204.pdf
    KeyPairGenerator g = KeyPairGenerator.getInstance("ML-DSA-87");
    return g.generateKeyPair();
  }
}
