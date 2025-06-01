package cn.xilio.netty.demo2;



import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
/**
 * 复现粘包问题
 */
public class NioClient {
    public static void main(String[] args) throws IOException, InterruptedException {
        SocketChannel clientChannel = SocketChannel.open();
        clientChannel.connect(new InetSocketAddress("localhost", 8080));

        // 快速发送两条消息（可能被合并）
        clientChannel.write(ByteBuffer.wrap("Hello".getBytes()));

        clientChannel.write(ByteBuffer.wrap("World".getBytes()));
    }
}
