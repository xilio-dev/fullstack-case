package cn.xilio.netty.demo5;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;

public class ChatServer {
    // 保存所有客户端通道
    private static final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    // 定义属性键，用于存储客户端昵称
    private static final AttributeKey<String> NICKNAME = AttributeKey.valueOf("nickname");

    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new StringDecoder());
                            pipeline.addLast(new StringEncoder());
                            pipeline.addLast(new ChatServerHandler());
                        }
                    });
            ChannelFuture f = b.bind(8080).sync();
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    static class ChatServerHandler extends SimpleChannelInboundHandler<String> {
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            // 新客户端连接，添加到 ChannelGroup
            channelGroup.add(ctx.channel());
            // 设置默认昵称
            ctx.channel().attr(NICKNAME).set("User_" + ctx.channel().id().asShortText());
            // 广播新用户加入
            channelGroup.writeAndFlush(ctx.channel().attr(NICKNAME).get() + " joined the chat!");
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
            // 获取发送者的昵称
            String nickname = ctx.channel().attr(NICKNAME).get();
            // 如果消息以 "/nick " 开头，修改昵称
            if (msg.startsWith("/nick ")) {
                String newNickname = msg.substring(6).trim();
                ctx.channel().attr(NICKNAME).set(newNickname);
                ctx.writeAndFlush("Nickname changed to " + newNickname);
                return;
            }
            // 广播消息
            String broadcastMsg = nickname + ": " + msg;
            channelGroup.forEach(ch -> {
                if (ch != ctx.channel()) {
                    ch.writeAndFlush(broadcastMsg);
                } else {
                    ch.writeAndFlush("You: " + msg);
                }
            });
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            // 客户端断开，移除通道并广播
            channelGroup.remove(ctx.channel());
            channelGroup.writeAndFlush(ctx.channel().attr(NICKNAME).get() + " left the chat!");
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            // 异常处理
            cause.printStackTrace();
            ctx.close();
            channelGroup.writeAndFlush(ctx.channel().attr(NICKNAME).get() + " disconnected due to error!");
        }
    }
}
