package cn.xilio.netty.demo7_websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class CustomProtocolServer {
    // 自定义消息格式：4字节长度 + 内容
    static class CustomMessage {
        private int length;
        private String content;

        public CustomMessage(int length, String content) {
            this.length = length;
            this.content = content;
        }
    }

    // 自定义协议解码器
    static class CustomDecoder extends ByteToMessageDecoder {
        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
            //解决沾包问题
            if (in.readableBytes() >= 4) {
                int length = in.readInt(); // 读取长度
                if (in.readableBytes() >= length) {
                    byte[] content = new byte[length];
                    in.readBytes(content);
                    out.add(new CustomMessage(length, new String(content, StandardCharsets.UTF_8)));
                }
            }
        }
    }

    // 自定义协议编码器
    static class CustomEncoder extends MessageToByteEncoder<CustomMessage> {
        @Override
        protected void encode(ChannelHandlerContext ctx, CustomMessage msg, ByteBuf out) {
            out.writeInt(msg.length);
            out.writeBytes(msg.content.getBytes(StandardCharsets.UTF_8));
        }
    }

    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new CustomDecoder()); // 添加解码器
                            pipeline.addLast(new CustomEncoder()); // 添加编码器
                            pipeline.addLast(new SimpleChannelInboundHandler<CustomMessage>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, CustomMessage msg) {
                                    System.out.println("Received: " + msg.content);
                                    ctx.writeAndFlush(new CustomMessage(msg.content.length(), "Server: " + msg.content));
                                }
                            });
                        }
                    });

            ChannelFuture future = bootstrap.bind(8080).sync();
            System.out.println("Custom Protocol Server started on port 8080");
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
