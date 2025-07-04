package cn.xilio.netty.tls.single_dest;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import java.io.File;

public class NettyTls13Server {
    public static void main(String[] args) throws Exception {
        // 加载服务端证书和私钥
        File certFile = new File("server_cert.pem");
        File keyFile = new File("server_key.pem");

        // 配置支持 TLS 1.3 的 SSL 上下文
        SslContext sslContext = SslContextBuilder.forServer(certFile, keyFile)
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
                     pipeline.addLast(new StringDecoder());
                     pipeline.addLast(new StringEncoder());
                     // 添加业务逻辑处理器
                     pipeline.addLast(new SimpleChannelInboundHandler<String>() {
                         @Override
                         protected void channelRead0(ChannelHandlerContext ctx, String msg) {
                             System.out.println("Received from client: " + msg);
                             ctx.writeAndFlush("Hello, Client!"); // 回复客户端
                         }
                     });
                 }
             });

            ChannelFuture f = b.bind(8443).sync();
            System.out.println("Netty TLS 1.3 Server started on port 8443...");
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
