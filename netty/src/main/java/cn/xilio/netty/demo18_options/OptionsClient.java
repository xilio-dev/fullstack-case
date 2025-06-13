package cn.xilio.netty.demo18_options;

import cn.xilio.netty.demo1_start.TimeClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class OptionsClient {
    public static void main(String[] args) throws Exception {
        String host = "localhost";
        int port = 8200;
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap(); // (1)
            b.group(workerGroup); // (2)
            b.channel(NioSocketChannel.class); // (3)
            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new StringDecoder());
                    ch.pipeline().addLast(new StringEncoder());

                    ch.pipeline().addLast(new SimpleChannelInboundHandler<String>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
                            System.out.println(msg);
                        }
                    });
                }
            });

            ChannelFuture f = b.connect(host, port).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    System.out.println("连接成功");
                    future.channel().writeAndFlush("w");
                } else {
                    System.out.println("连接失败");
                    future.cause().printStackTrace();
                }
            }).sync();
            for (int i = 0; i < 1000; i++) {
                f.channel().writeAndFlush("测试水位线，测试水位线，测试水位线，测试水位线");
                Thread.sleep(10);
            }

            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();

        }
    }
}
