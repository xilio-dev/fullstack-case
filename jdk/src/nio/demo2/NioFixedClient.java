package nio.demo2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class NioFixedClient {
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

        // 3. 创建缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        Scanner scanner = new Scanner(System.in);

        while (true) {
            // 4. 用户输入消息
            System.out.print("请输入要发送的消息 (输入 'exit' 退出): ");
            String message = scanner.nextLine();
            if ("exit".equalsIgnoreCase(message)) {
                break;
            }

            // 5. 发送消息，前面加上 4 字节长度
            buffer.clear();
            byte[] msgBytes = message.getBytes(StandardCharsets.UTF_8);
            buffer.putInt(msgBytes.length); // 写入消息长度
            buffer.put(msgBytes); // 写入消息内容
            buffer.flip();
            socketChannel.write(buffer);
            System.out.println("发送消息: " + message);

            // 6. 读取服务端响应
            buffer.clear();
            int bytesRead = socketChannel.read(buffer);
            if (bytesRead > 0) {
                buffer.flip();
                while (buffer.hasRemaining() && buffer.remaining() >= 4) {
                    int length = buffer.getInt();
                    if (buffer.remaining() >= length) {
                        byte[] data = new byte[length];
                        buffer.get(data);
                        String response = new String(data, StandardCharsets.UTF_8);
                        System.out.println("服务端响应: " + response);
                    } else {
                        buffer.reset(); // 恢复到长度字段之前
                        break;
                    }
                }
                buffer.compact(); // 保留未处理数据
            }
        }

        // 7. 关闭连接
        socketChannel.close();
        scanner.close();
        System.out.println("客户端关闭");
    }
}
