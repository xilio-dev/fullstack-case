package nio.demo2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class NioFixedServer {
    // 存储每个客户端的缓冲区，用于处理半包
    private static final Map<SocketChannel, ByteBuffer> clientBuffers = new HashMap<>();

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
                    // 为新客户端分配缓冲区
                    clientBuffers.put(client, ByteBuffer.allocate(4096));
                    client.register(selector, SelectionKey.OP_READ);
                    System.out.println("客户端连接: " + client.getRemoteAddress());
                }
                // 7. 处理读事件
                else if (key.isReadable()) {
                    SocketChannel client = (SocketChannel) key.channel();
                    ByteBuffer buffer = clientBuffers.get(client);
                    try {
                        int bytesRead = client.read(buffer);
                        if (bytesRead == -1) {
                            client.close();
                            clientBuffers.remove(client);
                            System.out.println("客户端断开: " + client.getRemoteAddress());
                        } else {
                            buffer.flip();
                            // 处理缓冲区数据
                            while (buffer.hasRemaining()) {
                                // 确保至少有 4 字节读取长度
                                if (buffer.remaining() >= 4) {
                                    // 记录当前位置，读取长度
                                    buffer.mark();
                                    int length = buffer.getInt();
                                    // 检查是否有足够的数据
                                    if (length <= buffer.remaining()) {
                                        // 读取完整消息
                                        byte[] data = new byte[length];
                                        buffer.get(data);
                                        String message = new String(data, StandardCharsets.UTF_8);
                                        System.out.println("收到完整消息: " + message);

                                        // 回显消息
                                        ByteBuffer responseBuffer = ByteBuffer.allocate(4 + message.length());
                                        responseBuffer.putInt(message.length());
                                        responseBuffer.put(message.getBytes(StandardCharsets.UTF_8));
                                        responseBuffer.flip();
                                        client.write(responseBuffer);
                                    } else {
                                        // 数据不足，恢复到长度字段之前，等待下次读
                                        buffer.reset();
                                        break;
                                    }
                                } else {
                                    // 长度字段不足，等待下次读取
                                    break;
                                }
                            }
                            // 压缩缓冲区，保留未处理数据
                            buffer.compact();
                        }
                    } catch (IOException e) {
                        client.close();
                        clientBuffers.remove(client);
                        System.out.println("客户端异常断开: " + client.getRemoteAddress());
                    }
                }

                // 8. 移除已处理事件
                iterator.remove();
            }
        }
    }
}
