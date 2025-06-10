package cn.xilio.netty.demo14_tls.doubel_dest;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import java.io.File;

public class NettyTls13MutualAuthClient {
    public static void main(String[] args) throws Exception {
        // 加载客户端证书和私钥
        File clientCert = new File("client_cert.pem");
        File clientKey = new File("client_key.pem");

        // 加载服务端证书（用于验证服务端）
        File serverCert = new File("server_cert.pem");

        // 配置 SSL 上下文
        SslContext sslContext = SslContextBuilder.forClient()
                .keyManager(clientCert, clientKey) // 客户端证书和私钥
                .trustManager(serverCert) // 信任服务端证书
                .protocols("TLSv1.3") // 指定使用 TLS 1.3
                .build();

        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioSocketChannel.class)
             .handler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 protected void initChannel(SocketChannel ch) {
                     ChannelPipeline pipeline = ch.pipeline();
                     // 添加 SSL 处理器
                     pipeline.addLast(sslContext.newHandler(ch.alloc()));
                     // 添加业务逻辑处理器
                     pipeline.addLast(new SimpleChannelInboundHandler<String>() {
                         @Override
                         public void channelActive(ChannelHandlerContext ctx) {
                             System.out.println("已连接到服务端...");
                             ctx.writeAndFlush("你好，服务端！");
                         }

                         @Override
                         protected void channelRead0(ChannelHandlerContext ctx, String msg) {
                             System.out.println("收到服务端消息: " + msg);
                         }
                     });
                 }
             });

            ChannelFuture f = b.connect("localhost", 8443).sync();
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}
