package org.xxdc.oss.example.security;

import static org.testng.Assert.assertNotNull;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import javax.crypto.KEM;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests the functionality of the KyberKEMSpi class, which is an implementation of the
 * Kyber Key Encapsulation Mechanism (KEM) algorithm.
 *
 * The tests cover the following scenarios:
 * - Verifying that a new instance of KyberKEMSpi can be created successfully.
 * - Verifying that the Kyber KEM algorithm can be loaded and used through the
 *   Java Security API.
 * 
 * Note: This may fail if the Kyber KEM algorithm is not registered with the Java Security API due
 * to java security provider issues and or if the jar file has not been signed with the
 * correct key. The Temurin JDK does not worry about this.
 */
public class KyberKEMSpiTest {

  @Test
  public void testCanCreateKyberKEMSpi() {
    KyberKEMSpi kyberKEMSpi = new KyberKEMSpi();
    assertNotNull(kyberKEMSpi);
  }

  @Test
  public void testCanLoadRegisteredKyberKEMSpi() {
    Security.addProvider(new KyberKEMProvider());
    try {
      var kem = KEM.getInstance("Kyber", "BCPQC.KEM");
      assertNotNull(kem);
    } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
      Assert.fail("Kyber KEM should be available", e);
    }
  }
}
