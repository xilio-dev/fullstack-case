package cn.xilio.netty.http2;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http2.*;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.util.CharsetUtil;

public class Http2Server {
    static final int PORT = 8443;

    public static void main(String[] args) throws Exception {
        // 1. 生成自签名证书（生产环境需替换为正式证书）
        SelfSignedCertificate ssc = new SelfSignedCertificate();
        SslContext sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey())
                .applicationProtocolConfig(new ApplicationProtocolConfig(
                        ApplicationProtocolConfig.Protocol.ALPN,
                        ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                        ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                        "h2")) // 强制使用HTTP/2
                .build();

        // 2. 配置线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) {
                            // 添加SSL和HTTP/2处理器
                            ch.pipeline().addLast(sslCtx.newHandler(ch.alloc()));
                            ch.pipeline().addLast(Http2FrameCodecBuilder.forServer().build());// 服务端使用forServer()
                            ch.pipeline().addLast(new Http2ServerHandler());
                        }
                    });

            // 3. 绑定端口并启动
            ChannelFuture f = b.bind(PORT).sync();
            System.out.println("HTTP/2 Server started on port " + PORT);
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}

// HTTP/2请求处理器
class Http2ServerHandler extends SimpleChannelInboundHandler<Http2DataFrame> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Http2DataFrame msg) {
        // 处理请求数据
        String requestData = msg.content().toString(io.netty.util.CharsetUtil.UTF_8);
        System.out.println("Server received: " + requestData);

        // 构造HTTP/2响应
        Http2Headers headers = new DefaultHttp2Headers().status("200");
        ctx.write(new DefaultHttp2HeadersFrame(headers));
        ctx.writeAndFlush(new DefaultHttp2DataFrame(
                Unpooled.copiedBuffer("Hello from HTTP/2 Server", CharsetUtil.UTF_8)));
    }
}
