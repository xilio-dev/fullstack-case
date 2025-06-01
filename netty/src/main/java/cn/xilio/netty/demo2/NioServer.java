package cn.xilio.netty.demo2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
/**
 * 复现粘包问题
 */
public class NioServer {
    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(8080));
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        while (true) {
            SocketChannel clientChannel = serverChannel.accept();
            int len = clientChannel.read(buffer);
            if (len > 0) {
                buffer.flip();
                byte[] data = new byte[buffer.remaining()];
                buffer.get(data);
                System.out.println("收到数据: " + new String(data)); // 可能输出 "HelloWorld"（粘包）
                buffer.clear();
            }
        }
    }
}
