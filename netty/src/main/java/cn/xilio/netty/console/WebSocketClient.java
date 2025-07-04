package cn.xilio.netty.console;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;

import java.net.URI;
import java.util.Scanner;

/**
 * 命令行终端与服务器连接事实交互命令
 */
public class WebSocketClient {
    static final String SERVER_URI = "ws://localhost:8080/command";

    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        // HTTP编解码器
                        pipeline.addLast(new HttpClientCodec());
                        // 聚合HTTP响应
                        pipeline.addLast(new HttpObjectAggregator(65536));
                        // WebSocket客户端协议处理器
                        pipeline.addLast(new WebSocketClientProtocolHandler(
                            URI.create(SERVER_URI),
                            WebSocketVersion.V13,
                            null,
                            false,
                            new DefaultHttpHeaders(),
                            5000
                        ));
                        // 自定义响应处理器
                        pipeline.addLast(new ClientResponseHandler());
                    }
                });

            ChannelFuture future = bootstrap.connect(
                new URI(SERVER_URI).getHost(),
                new URI(SERVER_URI).getPort()
            ).sync();

            // 用户输入命令
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("输入命令（或exit退出）: ");
                String input = scanner.nextLine();
                if ("exit".equalsIgnoreCase(input)) break;

                // 发送命令到服务端
                if (future.channel().isActive()) {
                    future.channel().writeAndFlush(new TextWebSocketFrame(input));
                } else {
                    System.err.println("连接未就绪");
                }
            }
            scanner.close();
            future.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    // 处理服务端响应
    private static class ClientResponseHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
            System.out.println("服务端响应: " + frame.text());
        }
    }
}
