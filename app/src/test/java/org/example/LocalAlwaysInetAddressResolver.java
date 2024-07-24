package org.example;

import java.net.InetAddress;
import java.net.spi.InetAddressResolver;
import java.util.stream.Stream;
import java.net.UnknownHostException;

public class LocalAlwaysInetAddressResolver implements InetAddressResolver {
    @Override
    public Stream<InetAddress> lookupByName(String host, LookupPolicy lookupPolicy)
            throws UnknownHostException {
        return Stream.of(InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 }));
    }

    @Override
    public String lookupByAddress(byte[] addr) {
        return "localhost";
    }
}
