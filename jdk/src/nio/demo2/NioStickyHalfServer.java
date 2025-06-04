package nio.demo2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class NioStickyHalfServer {
    public static void main(String[] args) throws IOException {
        // 1. 创建 Selector
        Selector selector = Selector.open();

        // 2. 创建 ServerSocketChannel，绑定端口
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress("localhost", 8080));
        serverSocket.configureBlocking(false);

        // 3. 注册到 Selector，监听接受连接事件
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("服务端启动，监听端口 8080...");

        while (true) {
            // 4. 等待事件
            selector.select();

            // 5. 处理就绪事件
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectedKeys.iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();

                // 6. 处理连接事件
                if (key.isAcceptable()) {
                    ServerSocketChannel server = (ServerSocketChannel) key.channel();
                    SocketChannel client = server.accept();
                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_READ);
                    System.out.println("客户端连接: " + client.getRemoteAddress());
                }
                // 7. 处理读事件
                else if (key.isReadable()) {
                    SocketChannel client = (SocketChannel) key.channel();
                    // 使用小缓冲区（仅 10 字节）模拟半包 如果客户端发送数据包太长会被拆分
                    ByteBuffer buffer = ByteBuffer.allocate(10);
                    try {
                        int bytesRead = client.read(buffer);
                        if (bytesRead == -1) {
                            client.close();
                            System.out.println("客户端断开: " + client.getRemoteAddress());
                        } else {
                            buffer.flip();
                            String received = StandardCharsets.UTF_8.decode(buffer).toString();
                            // 打印原始接收数据，观察粘包或半包
                            System.out.println("收到原始数据: [" + received + "]");
                        }
                    } catch (IOException e) {
                        client.close();
                        System.out.println("客户端异常断开: " + client.getRemoteAddress());
                    }
                }

                // 8. 移除已处理事件
                iterator.remove();
            }
        }
    }
}
