package cn.xilio.netty.demo2_nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * 解决粘包问题
 */
public class FixedNioClient {
    public static void main(String[] args) throws IOException {
        SocketChannel clientChannel = SocketChannel.open();
        clientChannel.connect(new InetSocketAddress("localhost", 8080));

        // 连续发送带长度头的消息
        sendMessage(clientChannel, "He");
        sendMessage(clientChannel, "World");
    }

    private static void sendMessage(SocketChannel channel, String msg) throws IOException {
        byte[] data = msg.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(4 + data.length);
        buffer.putInt(data.length); // 写入长度头
        buffer.put(data);           // 写入消息体
        buffer.flip();
        channel.write(buffer);
    }
}
