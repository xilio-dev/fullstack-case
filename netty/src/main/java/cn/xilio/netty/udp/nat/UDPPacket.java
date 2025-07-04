package cn.xilio.netty.udp.nat;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;

public class UDPPacket {
    private String remoteAddr; // Source/destination address
    private byte[] content; // Packet payload

    public UDPPacket(String remoteAddr, byte[] content) {
        this.remoteAddr = remoteAddr;
        this.content = content;
    }

    public String getRemoteAddr() { return remoteAddr; }
    public byte[] getContent() { return content; }

    // Encode to ByteBuf for TCP transmission
    public ByteBuf encode() {
        byte[] addrBytes = remoteAddr.getBytes(StandardCharsets.UTF_8);
        ByteBuf buf = Unpooled.buffer();
        int totalLength = 4 + addrBytes.length + content.length;
        buf.writeInt(totalLength);
        buf.writeInt(addrBytes.length);
        buf.writeBytes(addrBytes);
        buf.writeBytes(content);
        return buf;
    }

    // Decode from ByteBuf
    public static UDPPacket decode(ByteBuf buf) {
        int totalLength = buf.readInt();
        System.out.println("Decoding UDPPacket, totalLength: " + totalLength);
        if (totalLength < 8 || totalLength > buf.readableBytes() + 4) {
            throw new IllegalArgumentException("Invalid totalLength: " + totalLength);
        }
        int addrLength = buf.readInt();
        System.out.println("Decoding UDPPacket, addrLength: " + addrLength);
        if (addrLength < 0 || addrLength > totalLength - 4) {
            throw new IllegalArgumentException("Invalid addrLength: " + addrLength);
        }
        byte[] addrBytes = new byte[addrLength];
        buf.readBytes(addrBytes);
        String remoteAddr = new String(addrBytes, StandardCharsets.UTF_8);
        byte[] content = new byte[totalLength - 4 - addrLength];
        buf.readBytes(content);
        return new UDPPacket(remoteAddr, content);
    }
}
