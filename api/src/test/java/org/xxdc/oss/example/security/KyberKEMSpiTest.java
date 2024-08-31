package org.xxdc.oss.example.security;

import static org.testng.Assert.assertNotNull;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import javax.crypto.KEM;
import org.testng.Assert;
import org.testng.annotations.Test;

public class KyberKEMSpiTest {

  @Test
  public void testCanCreateKyberKEMSpi() {
    KyberKEMSpi kyberKEMSpi = new KyberKEMSpi();
    assertNotNull(kyberKEMSpi);
  }

  @Test
  public void testCanLoadRegisterKyberKEMSpi() {
    Security.addProvider(new KyberKEMProvider());
    try {
      var kem = KEM.getInstance("Kyber", "BCPQC.KEM");
      assertNotNull(kem);
    } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
      Assert.fail("Kyber KEM should be available", e);
    }
  }
}
