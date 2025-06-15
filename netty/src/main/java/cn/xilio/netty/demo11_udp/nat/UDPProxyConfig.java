package cn.xilio.netty.demo11_udp.nat;

public class UDPProxyConfig {
    private String proxyBindAddr = "127.0.0.1"; // 绑定到 localhost
    private int remotePort = 7000; // 公网 UDP 端口
    private int controlPort = 7001; // TCP 控制端口
    private int udpPacketSize = 65535; // 最大 UDP 数据包大小

    // Getters and setters
    public String getProxyBindAddr() { return proxyBindAddr; }
    public void setProxyBindAddr(String proxyBindAddr) { this.proxyBindAddr = proxyBindAddr; }
    public int getRemotePort() { return remotePort; }
    public void setRemotePort(int remotePort) { this.remotePort = remotePort; }
    public int getControlPort() { return controlPort; }
    public void setControlPort(int controlPort) { this.controlPort = controlPort; }
    public int getUdpPacketSize() { return udpPacketSize; }
    public void setUdpPacketSize(int udpPacketSize) { this.udpPacketSize = udpPacketSize; }
}
