package cn.xilio.jdk.nio.demo1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class NioClient {
    public static void main(String[] args) throws IOException {
        // 1. 创建 SocketChannel 并连接到服务端
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false); // 设置非阻塞模式
        socketChannel.connect(new InetSocketAddress("localhost", 8080));

        // 2. 完成连接
        while (!socketChannel.finishConnect()) {
            // 等待连接完成
            Thread.yield();
        }
        System.out.println("已连接到服务端");

        // 3. 创建缓冲区用于接收服务端响应
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        Scanner scanner = new Scanner(System.in);

        while (true) {
            // 4. 用户输入消息
            System.out.print("请输入要发送的消息 (输入 'exit' 退出): ");
            String message = scanner.nextLine();
            if ("exit".equalsIgnoreCase(message)) {
                break;
            }

            // 5. 发送消息到服务端
            buffer.clear();
            buffer.put(message.getBytes(StandardCharsets.UTF_8));
            buffer.flip();
            socketChannel.write(buffer);

            // 6. 读取服务端响应
            buffer.clear();
            int bytesRead = socketChannel.read(buffer);
            if (bytesRead > 0) {
                buffer.flip();
                String response = StandardCharsets.UTF_8.decode(buffer).toString();
                System.out.println("服务端响应: " + response);
            }
        }

        // 7. 关闭连接
        socketChannel.close();
        scanner.close();
        System.out.println("客户端关闭");
    }
}
