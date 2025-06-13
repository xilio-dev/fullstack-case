package cn.xilio.netty.demo19_water_line;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.util.concurrent.TimeUnit;
//todo 没完全明白 水位线
public class NettyClient {
    public static void main(String[] args) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                     .channel(NioSocketChannel.class)
                     .handler(new ChannelInitializer<Channel>() {
                         @Override
                         protected void initChannel(Channel ch) {
                             ch.pipeline()
                               .addLast(new LengthFieldBasedFrameDecoder(1024, 0, 4))
                               .addLast(new ClientHandler());
                         }
                     });

            ChannelFuture future = bootstrap.connect("localhost", 8080).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

    private static class ClientHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            // 模拟慢消费：延迟1秒处理每条消息
            ctx.channel().eventLoop().schedule(() -> {
                System.out.println("客户端：已接收数据，延迟后发布");
                ((ByteBuf) msg).release(); // 释放内存
            }, 1, TimeUnit.SECONDS);
        }
    }
}
