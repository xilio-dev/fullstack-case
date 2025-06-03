package cn.xilio.netty.demo4;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

public class ProtobufClient {
    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioSocketChannel.class)
             .handler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 protected void initChannel(SocketChannel ch) {
                     ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());
                     ch.pipeline().addLast(new ProtobufDecoder(
                         MessageProto.DataMessage.getDefaultInstance()));
                     ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
                     ch.pipeline().addLast(new ProtobufEncoder());
                     ch.pipeline().addLast(new ClientHandler());
                 }
             });

            Channel channel = b.connect("localhost", 8082).sync().channel();
            System.out.println("Connected to server");

            // 发送测试消息
            MessageProto.DataMessage request = MessageProto.DataMessage.newBuilder()
                .setId(100)
                .setContent("你就是个")
                .addTags("数据库")
                .addTags("人工智能")
                .build();

            channel.writeAndFlush(request);
            channel.closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}
