package cn.xilio.netty.demo7;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

//ws://127.0.0.1:8080/ws
public class HeartbeatServer {
    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            // 10秒未收到读事件触发IdleStateEvent 客户端连接了，但是没有发送数据 如果没有收到可读的事件，
                            pipeline.addLast(new IdleStateHandler(10, 0, 0, TimeUnit.SECONDS));
                            pipeline.addLast(new SimpleChannelInboundHandler<Object>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
                                    System.out.println("Received: " + msg);
                                }

                                @Override
                                public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
                                    if (evt instanceof IdleStateEvent) {
                                        IdleStateEvent event = (IdleStateEvent) evt;
                                        if (event.state() == IdleState.READER_IDLE) {
                                            System.out.println("Client timeout, closing connection");
                                            ctx.close(); // 超时关闭连接 会断开与客户端的链接
                                        }
                                    }
                                }
                            });
                        }
                    });

            ChannelFuture future = bootstrap.bind(8080).sync();
            System.out.println("Heartbeat Server started on port 8080");
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
