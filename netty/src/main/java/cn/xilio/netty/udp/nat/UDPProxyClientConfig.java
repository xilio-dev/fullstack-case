package cn.xilio.netty.udp.nat;

public class UDPProxyClientConfig {
    private String serverAddr = "127.0.0.1"; // 服务器地址
    private int serverControlPort = 7001; // 服务器 TCP 控制端口
    private String localAddr = "127.0.0.1"; // 本地 UDP 监听地址
    private int localPort = 9000; // 本地 UDP 端口
    private int udpPacketSize = 65535;

    // Getters and setters
    public String getServerAddr() { return serverAddr; }
    public void setServerAddr(String serverAddr) { this.serverAddr = serverAddr; }
    public int getServerControlPort() { return serverControlPort; }
    public void setServerControlPort(int serverControlPort) { this.serverControlPort = serverControlPort; }
    public String getLocalAddr() { return localAddr; }
    public void setLocalAddr(String localAddr) { this.localAddr = localAddr; }
    public int getLocalPort() { return localPort; }
    public void setLocalPort(int localPort) { this.localPort = localPort; }
    public int getUdpPacketSize() { return udpPacketSize; }
    public void setUdpPacketSize(int udpPacketSize) { this.udpPacketSize = udpPacketSize; }
}
