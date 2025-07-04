package cn.xilio.netty.ssl.example1;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import java.io.File;
//单向和双向ssl认证 警告：SSL 3.0已经被弃用，建议使用TLS 1.2/1.3
public class NettySslServerManual {
    public static void main(String[] args) throws Exception {
        // 加载服务端证书和私钥
        File certFile = new File("server_cert.pem");
        File keyFile = new File("server_key.pem");

        // 配置 SSL 上下文
//        SslContext sslContext = SslContextBuilder
//                .forServer(certFile, keyFile)
//                .build();
        //双向认证
        SslContext sslContext = SslContextBuilder.forServer(certFile, keyFile)
                .trustManager(new File("client_cert.pem")) // 信任客户端证书
                .clientAuth(ClientAuth.REQUIRE) // 强制客户端提供证书
                .build();

        // 配置 Netty 服务端
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
                     pipeline.addLast(new StringDecoder());
                     pipeline.addLast(new StringEncoder());
                     // 添加业务处理器
                     pipeline.addLast(new SimpleChannelInboundHandler<String>() {
                         @Override
                         protected void channelRead0(ChannelHandlerContext ctx, String msg) {
                             System.out.println("Received from client: " + msg);
                             ctx.writeAndFlush("Hello, Client!");
                         }

                         @Override
                         public void channelActive(ChannelHandlerContext ctx) throws Exception {
                             ctx.channel().writeAndFlush("Welcome to Netty SSL Server!");
                             super.channelActive(ctx);
                         }

                         @Override
                         public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                             super.exceptionCaught(ctx, cause);
                         }
                     });
                 }
             });

            ChannelFuture f = b.bind(8443).sync();
            System.out.println("Netty SSL Server started on port 8443.");
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
