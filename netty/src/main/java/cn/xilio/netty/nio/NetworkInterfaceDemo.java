package cn.xilio.netty.nio;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class NetworkInterfaceDemo {
    public static void main(String[] args) throws SocketException {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            System.out.println(networkInterface.getName());
            System.out.println(networkInterface.getDisplayName());
            System.out.println(networkInterface.getMTU());
            System.out.println("\n");
        }
    }
}
