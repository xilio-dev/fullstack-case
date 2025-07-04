package cn.xilio.netty.tls.doubel_dest;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import java.io.File;

public class NettyTls13MutualAuthServer {
    public static void main(String[] args) throws Exception {
        // 加载服务端证书和私钥
        File serverCert = new File("server_cert.pem");
        File serverKey = new File("server_key.pem");

        // 加载客户端证书（用于验证客户端）
        File clientCert = new File("client_cert.pem");

        // 配置 SSL 上下文
        SslContext sslContext = SslContextBuilder.forServer(serverCert, serverKey)
                .trustManager(clientCert) // 信任客户端证书
                .clientAuth(ClientAuth.REQUIRE) // 强制客户端提供证书
                .protocols("TLSv1.3") // 指定使用 TLS 1.3
                .build();

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .childHandler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 protected void initChannel(SocketChannel ch) {
                     ChannelPipeline pipeline = ch.pipeline();
                     // 添加 SSL 处理器
                     pipeline.addLast(sslContext.newHandler(ch.alloc()));
                     // 添加业务处理器
                     pipeline.addLast(new SimpleChannelInboundHandler<String>() {
                         @Override
                         protected void channelRead0(ChannelHandlerContext ctx, String msg) {
                             System.out.println("收到客户端消息: " + msg);
                             ctx.writeAndFlush("你好，客户端！");
                         }
                     });
                 }
             });

            ChannelFuture f = b.bind(8443).sync();
            System.out.println("Netty TLS 1.3 双向认证服务端已启动，监听端口 8443...");
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
