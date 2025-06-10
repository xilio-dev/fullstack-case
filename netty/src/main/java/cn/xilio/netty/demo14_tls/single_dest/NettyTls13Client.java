package cn.xilio.netty.demo14_tls.single_dest;

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

public class NettyTls13Client {
    public static void main(String[] args) throws Exception {
        // 加载服务端的信任证书
        File trustCertFile = new File("server_cert.pem");

        // 配置支持 TLS 1.3 的 SSL 上下文
        SslContext sslContext = SslContextBuilder.forClient()
                .trustManager(trustCertFile) // 信任服务端证书
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
                     pipeline.addLast(new StringDecoder());
                     pipeline.addLast(new StringEncoder());
                     // 添加业务逻辑处理器
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
