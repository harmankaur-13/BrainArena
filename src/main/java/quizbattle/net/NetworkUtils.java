package quizbattle.net;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Utility for discovering Wi-Fi / LAN IP addresses.
 */
public final class NetworkUtils {

    public static final int DEFAULT_PORT = 5555;
    public static final int DISCOVERY_PORT = 5556;

    private NetworkUtils() {}

    public static List<String> getLocalIPv4Addresses() {
        List<String> addresses = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp() || iface.isVirtual()) continue;
                Enumeration<InetAddress> inetAddresses = iface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress addr = inetAddresses.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        String ip = addr.getHostAddress();
                        if (!addresses.contains(ip)) addresses.add(ip);
                    }
                }
            }
        } catch (Exception ignored) {}

        if (addresses.isEmpty()) {
            try {
                InetAddress localHost = InetAddress.getLocalHost();
                if (localHost instanceof Inet4Address && !localHost.isLoopbackAddress()) {
                    addresses.add(localHost.getHostAddress());
                }
            } catch (Exception ignored) {}
        }
        return addresses;
    }

    public static String getPrimaryLocalIP() {
        List<String> ips = getLocalIPv4Addresses();
        for (String ip : ips) {
            if (ip.startsWith("192.168.") || ip.startsWith("10.")) return ip;
        }
        for (String ip : ips) {
            if (ip.startsWith("172.")) return ip;
        }
        return ips.isEmpty() ? "127.0.0.1" : ips.get(0);
    }
}

