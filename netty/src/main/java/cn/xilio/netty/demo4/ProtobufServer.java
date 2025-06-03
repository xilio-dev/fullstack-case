package cn.xilio.netty.demo4;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

public class ProtobufServer {
    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .childHandler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 protected void initChannel(SocketChannel ch) {
                     // Protobuf解码器（处理粘包）
                     ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());
                     ch.pipeline().addLast(new ProtobufDecoder(
                         MessageProto.DataMessage.getDefaultInstance()));

                     // Protobuf编码器
                     ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
                     ch.pipeline().addLast(new ProtobufEncoder());

                     // 业务处理器
                     ch.pipeline().addLast(new ServerHandler());
                 }
             });

            ChannelFuture f = b.bind(8082).sync();
            System.out.println("Server started on port 8080");
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
