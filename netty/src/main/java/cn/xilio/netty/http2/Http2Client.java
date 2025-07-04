package cn.xilio.netty.http2;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http2.*;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.CharsetUtil;

public class Http2Client {
    static final String HOST = "localhost";
    static final int PORT = 8443;

    public static void main(String[] args) throws Exception {
        // 1. 配置SSL上下文（忽略证书验证，仅用于测试）
        SslContext sslCtx = SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .applicationProtocolConfig(new ApplicationProtocolConfig(
                        ApplicationProtocolConfig.Protocol.ALPN,
                        ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                        ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                        "h2"))
                .build();

        // 2. 配置线程组
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) {
                            // 添加SSL和HTTP/2处理器
                            ch.pipeline().addLast(sslCtx.newHandler(ch.alloc(), HOST, PORT));
                            ch.pipeline().addLast(Http2FrameCodecBuilder.forClient().build()) ;// 客户端使用forClient()
                            ch.pipeline().addLast(new Http2ClientHandler());
                        }
                    });

            // 3. 连接服务器
            ChannelFuture f = b.connect(HOST, PORT).sync();
            System.out.println("Connected to HTTP/2 Server");

            // 4. 发送HTTP/2请求
            Http2Headers headers = new DefaultHttp2Headers()
                    .method("GET")
                    .path("/")
                    .scheme("https");
            f.channel().writeAndFlush(new DefaultHttp2HeadersFrame(headers));
            f.channel().writeAndFlush(new DefaultHttp2DataFrame(
                    Unpooled.copiedBuffer("Hello from HTTP/2 Client", CharsetUtil.UTF_8)));
            System.out.println("Sent HTTP/2 request");
            // 等待连接关闭
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}

// HTTP/2响应处理器
class Http2ClientHandler extends SimpleChannelInboundHandler<Http2DataFrame> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Http2DataFrame msg) {
        // 处理服务器响应
        String response = msg.content().toString(CharsetUtil.UTF_8);
        System.out.println("Client received: " + response);
    }
}
