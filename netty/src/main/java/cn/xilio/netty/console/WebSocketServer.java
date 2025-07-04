package cn.xilio.netty.console;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class WebSocketServer {
    static final int PORT = 8080;

    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        // HTTP编解码器
                        pipeline.addLast(new HttpServerCodec());
                        // 聚合HTTP请求
                        pipeline.addLast(new HttpObjectAggregator(65536));
                        // WebSocket协议处理器
                        pipeline.addLast(new WebSocketServerProtocolHandler("/command"));
                        // 自定义命令处理器
                        pipeline.addLast(new CommandHandler());
                    }
                });

            ChannelFuture future = bootstrap.bind(PORT).sync();
            System.out.println("WebSocket服务端启动，端口: " + PORT);
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    // 自定义命令处理器
    private static class CommandHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
            String command = frame.text();
            System.out.println("收到命令: " + command);

            // 处理命令（示例：add操作）
            if (command.startsWith("add")) {
                String response = "服务端执行成功: " + command.toUpperCase();
                ctx.writeAndFlush(new TextWebSocketFrame(response));
            } else {
                ctx.writeAndFlush(new TextWebSocketFrame("未知命令"));
            }
        }
    }
}
