package cn.xilio.netty.demo17_leak;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

public class NettyTestClient {
    public static void main(String[] args) throws InterruptedException {
        // 1. 创建线程组（1个线程足够）
        NioEventLoopGroup group = new NioEventLoopGroup(1);

        try {
            // 2. 创建客户端启动引导类
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class) // 使用NIO传输
                    .option(ChannelOption.TCP_NODELAY, true) // 禁用Nagle算法
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            // 3. 配置编解码器（字符串格式）
                            ch.pipeline()

                              .addLast(new ClientHandler()); // 业务处理器
                        }
                    });

            // 4. 连接服务端（假设服务端运行在本地8080端口）
            ChannelFuture future = bootstrap.connect("127.0.0.1", 8080).sync();
            System.out.println("客户端连接成功");

            // 5. 发送测试消息
            for (int i = 0; i < 10000; i++) {
                sendTestMessage(future.channel(), "Hello Server!我就是你的大哥哥牛不牛不Hello" +
                        " Server!我就是你的大哥哥牛不牛不Hello Server!我就是你的大哥哥牛不牛不Hello Server" +
                        " Server!我就是你的大哥哥牛不牛不Hello Server!我就是你的大哥哥牛不牛不Hello Server" +
                        "!我就是你的大哥哥牛不牛不Hello Server!我就是你的大哥哥牛不牛不Hello Server!我就是你的大哥哥牛不牛不Hello Server!我就是你的大哥哥牛不牛不Hello Server!我就是你的大哥哥牛不牛不Hello Server!我就是你的大哥哥牛不牛不Hello Server!我就是你的大哥哥牛不牛不Hello Server!我就是你的大哥哥牛不牛不Hello Server!我就是你的大哥哥牛不牛不Hello Server!我就是你的大哥哥牛不牛不Hello Server!我就是你的大哥哥牛不牛不Hello Server!我就是你的大哥哥牛不牛不Hello Server!我就是你的大哥哥牛不牛不Hello Server!我就是你的大哥哥牛不牛不Hello Server!我就是你的大哥哥牛不牛不Hello Server!我就是你的大哥哥牛不牛不Hello Server!我就是你的大哥哥牛不牛不Hello Server!我就是你的大哥哥牛不牛不Hello Server!我就是你的大哥哥牛不牛不Hello " +
                        "Server!我就是你的大哥哥牛不牛不Hello Server!我就是你的大哥哥牛不牛不Hello" +
                        " Server!我就是你的大哥哥牛不牛不Hello Server!我就是你的大哥哥牛不牛不"+i);
                Thread.sleep(10); // 控制发送速率
            }


            // 6. 等待连接关闭
            future.channel().closeFuture().sync();
        } finally {
            // 7. 优雅关闭线程组
            group.shutdownGracefully();
        }
    }

    // 封装消息发送方法（自动释放资源）
    private static void sendTestMessage(Channel channel, String msg) {
        if (channel.isActive()) {


            channel.writeAndFlush(Unpooled.wrappedBuffer(msg.getBytes())).addListener(future -> {
                if (future.isSuccess()) {
                    System.out.println("发送成功: " + msg);
                } else {
                    System.err.println("发送失败: " + future.cause());
                }
            });
        }
    }

    // 客户端处理器
    static class ClientHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            System.out.println("收到服务端响应: " + msg);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }
}
