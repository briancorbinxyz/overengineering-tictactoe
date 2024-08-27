package org.example;

import java.net.spi.InetAddressResolver;
import java.net.spi.InetAddressResolverProvider;

public class LocalAlwaysInetAddressResolverProvider extends InetAddressResolverProvider {
  @Override
  public InetAddressResolver get(Configuration configuration) {
    return new LocalAlwaysInetAddressResolver();
  }

  @Override
  public String name() {
    return "Local Always Inet Address Resolver Provider";
  }
}
