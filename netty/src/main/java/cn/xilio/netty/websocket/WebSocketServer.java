package cn.xilio.netty.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

public class WebSocketServer {
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
                            // 添加HTTP编解码器
                            pipeline.addLast(new HttpServerCodec());
                            // 聚合HTTP消息
                            pipeline.addLast(new HttpObjectAggregator(65536));
                            // 处理WebSocket协议
                            pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
                            // 自定义WebSocket消息处理器
                            pipeline.addLast(new SimpleChannelInboundHandler<TextWebSocketFrame>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
                                    String text = msg.text();
                                    System.out.println("Received: " + text);
                                    ctx.writeAndFlush(new TextWebSocketFrame("Server: " + text));
                                }

                                @Override
                                public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                     ctx.writeAndFlush("server:close");
                                     ctx.close();
                                }
                            });
                        }
                    });

            ChannelFuture future = bootstrap.bind(8080).sync();
            System.out.println("WebSocket Server started on port 8080");
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
