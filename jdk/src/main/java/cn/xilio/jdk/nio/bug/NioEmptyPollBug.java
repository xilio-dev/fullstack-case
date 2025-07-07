package cn.xilio.jdk.nio.bug;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NioEmptyPollBug {
    public static void main(String[] args) throws Exception {
        // 启动服务器
        new Thread(() -> {
            try {
                runServer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // 模拟客户端快速连接和断开
        Thread.sleep(1000); // 等待服务器启动
        for (int i = 0; i < 100; i++) {
            SocketChannel client = SocketChannel.open();
            client.connect(new InetSocketAddress("localhost", 8080));
            client.close(); // 立即关闭连接，增加触发 Bug 的概率
            Thread.sleep(10);
        }
    }

    private static void runServer() throws Exception {
        Selector selector = Selector.open();
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(8080));
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        int pollCount = 0;
        long lastTime = System.currentTimeMillis();

        while (true) {
            int selected = selector.select(1000); // 设置超时
            pollCount++;
            long currentTime = System.currentTimeMillis();

            // 检测空轮询：短时间内 select 返回次数过多
            if (currentTime - lastTime >= 1000) {
                System.out.println("Polls per second: " + pollCount);
                pollCount = 0;
                lastTime = currentTime;
            }

            if (selected == 0) {
                continue; // 空轮询可能在这里频繁发生
            }

            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
            while (keys.hasNext()) {
                SelectionKey key = keys.next();
                keys.remove();

                if (key.isAcceptable()) {
                    ServerSocketChannel server = (ServerSocketChannel) key.channel();
                    SocketChannel client = server.accept();
                    if (client != null) {
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ);
                        System.out.println("New client connected: " + client);
                    }
                } else if (key.isReadable()) {
                    SocketChannel client = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    try {
                        int bytesRead = client.read(buffer);
                        if (bytesRead == -1) {
                            client.close();
                            System.out.println("Client disconnected: " + client);
                        } else {
                            buffer.flip();
                            System.out.println("Received: " + new String(buffer.array(), 0, bytesRead));
                        }
                    } catch (Exception e) {
                        client.close();
                        System.out.println("Client error, closed: " + client);
                    }
                }
            }
        }
    }
}
