package org.epics.util.compat.jdk5.net;

import java.net.InetAddress;
import java.net.SocketException;
import java.util.*;

/**
 * Implementation of NetworkInterface networking class
 *
 * @author George McIntyre. 15-Feb-2021, SLAC
 */
public class NetworkInterface {
    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    private static final Map<NetworkInterface, Boolean> SUPPORTS_MULTICAST_CACHE = new HashMap<NetworkInterface, Boolean>();
    private static final String MULTICAST_PROBE_GROUP = "224.0.0.128";
    private static final int MULTICAST_PROBE_PORT = 2000;

    private final java.net.NetworkInterface networkInterface;

    public java.net.NetworkInterface getNetworkInterface() {
        return networkInterface;
    }

    public InetAddress getFirstIPV4Address() {
        List<InetAddress> addresses = Collections.list(getInetAddresses());
        for (InetAddress address : addresses) {
            InterfaceAddress interfaceAddress = new InterfaceAddress(address);
            if (interfaceAddress.isIPV4Address()) {
                return address;
            }
        }
        return null;
    }

    public NetworkInterface(java.net.NetworkInterface networkInterface) {
        this.networkInterface = networkInterface;
    }

    public int getIndex() throws SocketException {
        Enumeration<java.net.NetworkInterface> enumeratedNetworkInterfaces = java.net.NetworkInterface.getNetworkInterfaces();
        for (int i = 0; enumeratedNetworkInterfaces.hasMoreElements(); i++) {
            if (enumeratedNetworkInterfaces.nextElement().equals(this.networkInterface)) {
                return i;
            }
        }
        return -1;
    }

    public NetworkInterface getParent() {
        return null;
    }

    public boolean isUp() throws SocketException {
        Enumeration<InetAddress> enumeratedInetAddresses = this.networkInterface.getInetAddresses();
        return enumeratedInetAddresses.hasMoreElements();
    }

    public List<InterfaceAddress> getInterfaceAddresses() {
        Enumeration<InetAddress> enumeratedInetAddresses = this.networkInterface.getInetAddresses();
        List<InterfaceAddress> interfaceAddresses = new ArrayList<InterfaceAddress>();

        while (enumeratedInetAddresses.hasMoreElements()) {
            InetAddress inetAddress = enumeratedInetAddresses.nextElement();
            interfaceAddresses.add(new InterfaceAddress(inetAddress));
        }
        return Collections.unmodifiableList(interfaceAddresses);
    }

    public boolean isLoopback() {
        Enumeration<InetAddress> enumeratedInetAddresses = this.networkInterface.getInetAddresses();
        while (enumeratedInetAddresses.hasMoreElements()) {
            if (enumeratedInetAddresses.nextElement().isLoopbackAddress())
                return true;
        }
        return false;
    }

    public boolean isPointToPoint() {
        // TODO Find a good way to determine whether point to point
        return false;
    }

    /**
     * Does this interface support multicast.  Determine by trying to see if we can create and
     * join a multicast group on this channel
     *
     * @return true iff it supports multicast
     */
    public synchronized boolean supportsMulticast() {
        Boolean supportsMulticastCachedValue = SUPPORTS_MULTICAST_CACHE.get(this);
        if (supportsMulticastCachedValue != null) {
            return supportsMulticastCachedValue;
        }

        SUPPORTS_MULTICAST_CACHE.put(this, true);
        return true;
    }

    public boolean isVirtual() {
        // TODO Find a good way to determine whether isVirtual

        return false;
    }

    public byte[] getHardwareAddress() {
        // TODO Find a good way to determine the mac code
        return null;
    }

    public int getMTU() {
        // TODO Find a good way to determine the MTU
        //  For the moment we will return 1500 for all addresses
        //  except localhost where we will return 16384

        if (isLoopback()) {
            return 4168;
        } else {
            return 1500;
        }
    }

    public Enumeration<NetworkInterface> getSubInterfaces() {
        return Collections.enumeration(new ArrayList<NetworkInterface>());
    }

    /// DELEGATES

    public String getName() {
        return networkInterface.getName();
    }

    public Enumeration<InetAddress> getInetAddresses() {
        return networkInterface.getInetAddresses();
    }

    public String getDisplayName() {
        return networkInterface.getDisplayName();
    }

    public static Enumeration<NetworkInterface> getNetworkInterfaces() throws SocketException {
        Enumeration<java.net.NetworkInterface> enumeratedNetworkInterfaces = java.net.NetworkInterface.getNetworkInterfaces();
        List<NetworkInterface> networkInterfaceList = new ArrayList<NetworkInterface>();
        while (enumeratedNetworkInterfaces.hasMoreElements()) {
            networkInterfaceList.add(new NetworkInterface(enumeratedNetworkInterfaces.nextElement()));
        }
        return Collections.enumeration(networkInterfaceList);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NetworkInterface)) return false;

        NetworkInterface that = (NetworkInterface) o;

        return networkInterface != null ? networkInterface.equals(that.networkInterface) : that.networkInterface == null;
    }

    @Override
    public int hashCode() {
        return networkInterface != null ? networkInterface.hashCode() : 0;
    }

    @Override
    public String toString() {
        return networkInterface.toString();
    }
}
