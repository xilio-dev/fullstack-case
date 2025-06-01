package cn.xilio.netty.demo3;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.nio.charset.StandardCharsets;

public class PackProblemClient {
    public static void main(String[] args) throws Exception {
        Channel channel = new Bootstrap()
            .group(new NioEventLoopGroup())
            .channel(NioSocketChannel.class)
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    // 无任何处理
                }
            })
            .connect("localhost", 8080).sync().channel();

        // 快速发送3条消息（模拟粘包）
        for (int i = 0; i < 3; i++) {
            //Thread.sleep(1000);
            channel.writeAndFlush(Unpooled.copiedBuffer("msg" + i, StandardCharsets.UTF_8));
        }

        channel.close();
    }
}
