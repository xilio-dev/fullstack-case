package cn.xilio.netty.ssl.example1;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import java.io.File;
//单向和双向ssl认证 警告：SSL 3.0已经被弃用，建议使用TLS 1.2/1.3
public class NettySslClientManual {
    public static void main(String[] args) throws Exception {
        // 加载客户端证书和私钥
        File certFile = new File("client_cert.pem");
        File keyFile = new File("client_key.pem");
        File trustCertFile = new File("server_cert.pem"); // 信任的服务端证书

        // 配置 SSL 上下文
        SslContext sslContext = SslContextBuilder.forClient()
                .keyManager(certFile, keyFile) // 客户端证书和私钥
                .trustManager(trustCertFile) // 信任服务端的证书
                .build();

        // 配置 Netty 客户端
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
                     pipeline.addLast(new StringDecoder());
                     pipeline.addLast(new StringEncoder());
                     // 添加业务处理器
                     pipeline.addLast(new SimpleChannelInboundHandler<String>() {
                         @Override
                         public void channelActive(ChannelHandlerContext ctx) {
                             System.out.println("Connected to server...");
                             ctx.writeAndFlush("Hello, Server!");
                         }

                         @Override
                         protected void channelRead0(ChannelHandlerContext ctx, String msg) {
                             System.out.println("Received from server: " + msg);
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
