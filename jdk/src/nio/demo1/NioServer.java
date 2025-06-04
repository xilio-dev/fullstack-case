package nio.demo1;

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

public class NioServer {
    public static void main(String[] args) throws IOException {
        // 1. 创建 Selector，用于管理多个通道
        Selector selector = Selector.open();

        // 2. 创建 ServerSocketChannel，绑定端口
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress("localhost", 8080));
        // 设置为非阻塞模式
        serverSocket.configureBlocking(false);

        // 3. 将 ServerSocketChannel 注册到 Selector，监听接受连接事件
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("服务端启动，监听端口 8080...");

        while (true) {
            // 4. 阻塞等待事件发生
            selector.select();

            // 5. 获取所有就绪的事件
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectedKeys.iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();

                // 6. 处理接受连接事件
                if (key.isAcceptable()) {
                    // 获取 ServerSocketChannel
                    ServerSocketChannel server = (ServerSocketChannel) key.channel();
                    // 接受客户端连接
                    SocketChannel client = server.accept();
                    // 设置客户端通道为非阻塞
                    client.configureBlocking(false);
                    // 注册客户端通道到 Selector，监听读事件
                    client.register(selector, SelectionKey.OP_READ);
                    System.out.println("客户端连接: " + client.getRemoteAddress());
                }
                // 7. 处理读事件
                else if (key.isReadable()) {
                    SocketChannel client = (SocketChannel) key.channel();
                    // 创建缓冲区读取数据
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    try {
                        int bytesRead = client.read(buffer);
                        if (bytesRead == -1) {
                            // 客户端断开连接
                            client.close();
                            System.out.println("客户端断开: " + client.getRemoteAddress());
                        } else {
                            // 读取客户端发送的数据
                            buffer.flip();
                            String received = StandardCharsets.UTF_8.decode(buffer).toString();
                            System.out.println("收到消息: " + received);

                            // 回显消息给客户端
                            buffer.clear();
                            String response = "服务端收到: " + received;
                            buffer.put(response.getBytes(StandardCharsets.UTF_8));
                            buffer.flip();
                            client.write(buffer);
                        }
                    } catch (IOException e) {
                        // 异常处理，关闭连接
                        client.close();
                        System.out.println("客户端异常断开: " + client.getRemoteAddress());
                    }
                }

                // 8. 从已处理的事件集中移除当前事件
                iterator.remove();
            }
        }
    }
}
