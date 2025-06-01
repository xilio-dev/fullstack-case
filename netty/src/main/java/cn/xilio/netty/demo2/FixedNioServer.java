package cn.xilio.netty.demo2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
/**
 * 解决粘包问题
 */
public class FixedNioServer {
    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(8080));
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        while (true) {
            SocketChannel clientChannel = serverChannel.accept();
            while (clientChannel.read(buffer) > 0) {
                buffer.flip();
                while (buffer.remaining() > 4) { // 至少读取头部长度字段
                    int length = buffer.getInt(); // 读取消息长度
                    if (buffer.remaining() >= length) {
                        byte[] data = new byte[length];
                        buffer.get(data);
                        System.out.println("解析后的消息: " + new String(data));
                    } else {
                        buffer.rewind(); // 数据不足，等待下次读取
                        break;
                    }
                }
                buffer.compact();
            }
        }
    }
}
