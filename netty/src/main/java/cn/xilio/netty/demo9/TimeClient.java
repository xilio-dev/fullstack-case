package cn.xilio.netty.demo9;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;

public class TimeClient {
    public static void main(String[] args) throws Exception {
        String host = "localhost";
        int port = 3307;
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap(); // (1)
            b.group(workerGroup); // (2)
            b.channel(NioSocketChannel.class); // (3)

            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ProtobufDecoder(TunnelMessage.Message.getDefaultInstance()));
                    ch.pipeline().addLast(new ProtobufEncoder());
                    ch.pipeline().addLast(new ClientHandler());
                }
            });

            // Start the client.
            ChannelFuture f = b.connect(host, port).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        TunnelMessage.Message message = TunnelMessage.Message.newBuilder()
                                .setType(TunnelMessage.Message.Type.AUTH)
                                .setSerialNumber(1)
                                .setUri("123")
                                .build();
                        future.channel().writeAndFlush(message);

                    } else {
                        System.out.println("连接失败");
                        future.cause().printStackTrace();
                    }
                }
            }).sync();

            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();

        }
    }
}
