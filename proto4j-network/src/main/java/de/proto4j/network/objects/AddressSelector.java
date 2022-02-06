package de.proto4j.network.objects; //@date 30.01.2022

import de.proto4j.annotation.server.requests.selection.Selector;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class AddressSelector implements Selector {

    private final String hostAddress;

    public AddressSelector(String hostAddress) {this.hostAddress = hostAddress;}

    @Override
    public boolean canSelect(Object o) {
        if (!(o instanceof SocketAddress)) return false;

        if (o instanceof InetSocketAddress) {
            InetSocketAddress sa = (InetSocketAddress) o;

            return sa.getAddress().getHostAddress().equals(hostAddress);
        }
        return false;
    }
}
