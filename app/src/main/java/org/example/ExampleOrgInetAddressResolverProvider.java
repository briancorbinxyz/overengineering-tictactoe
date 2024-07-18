package org.example;

import java.net.spi.InetAddressResolver;
import java.net.spi.InetAddressResolverProvider;

public class ExampleOrgInetAddressResolverProvider extends InetAddressResolverProvider {
    @Override
    public InetAddressResolver get(Configuration configuration) {
        return new ExampleOrgInetAddressResolver();
    }

    @Override
    public String name() {
        return "Example.org Inet Address Resolver Provider";
    }
}
