package cn.xilio.netty.traffic.example3;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;

public class TrafficMonitorClient {
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            // 可以添加更多处理器
                        }
                    });

            Channel channel = bootstrap.connect("127.0.0.1", 8120).sync().channel();
            System.out.println("已连接到服务端...");

            // 模拟发送数据
            for (int i = 0; i < 100; i++) {
                String message = "Hello, this is message " + i;
                ByteBuf buffer = Unpooled.copiedBuffer(message, StandardCharsets.UTF_8);
                channel.writeAndFlush(buffer);
                Thread.sleep(500); // 每 500 毫秒发送一次
            }

            channel.closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}
