package nio.demo2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class NioStickyHalfClient {
    public static void main(String[] args) throws IOException, InterruptedException {
        // 1. 创建 SocketChannel 并连接
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress("localhost", 8080));

        // 2. 等待连接完成
        while (!socketChannel.finishConnect()) {
            Thread.yield();
        }
        System.out.println("已连接到服务端");

        // 3. 模拟粘包：快速发送多条短消息
        String[] messages = {"Hello", "World", "Java", "NIO"};
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        for (String msg : messages) {
            buffer.clear();
            buffer.put(msg.getBytes(StandardCharsets.UTF_8));
            buffer.flip();
            socketChannel.write(buffer);
            System.out.println("发送消息: " + msg);
        }

        // 4. 模拟半包：发送一个较长消息
        String longMessage = "ThisIsALongMessageToDemonstrateHalfPacketIssueInNIOCommunication";
        buffer.clear();
        buffer.put(longMessage.getBytes(StandardCharsets.UTF_8));
        buffer.flip();
        socketChannel.write(buffer);
        System.out.println("发送长消息: " + longMessage);

        // 5. 等待片刻后关闭
        Thread.sleep(1000);
        socketChannel.close();
        System.out.println("客户端关闭");
    }
}
