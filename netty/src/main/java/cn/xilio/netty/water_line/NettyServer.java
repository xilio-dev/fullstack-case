package cn.xilio.netty.water_line;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.concurrent.TimeUnit;
//todo 没完全明白 水位线
public class NettyServer {
    public static void main(String[] args) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    // 设置水位线（低8KB，高32KB）
                    .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK,
                            new WriteBufferWaterMark(1 * 1024, 4 * 1024))
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) {
                            ch.pipeline()
                                    .addLast(new ServerHandler());
                        }
                    });

            ChannelFuture future = bootstrap.bind(8080).sync();
            System.out.println("Server started. Press Ctrl+C to stop.");
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private static class ServerHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            System.out.println("Client connected. Start sending data...");
            sendData(ctx); // 开始发送数据
        }

        private void sendData(ChannelHandlerContext ctx) {
            if (ctx.channel().isWritable()) {
                ByteBuf data = ctx.alloc().buffer(1024); // 每次发送1KB
                data.writeBytes(new byte[1024]);
                ctx.writeAndFlush(data).addListener(future -> {
                    if (future.isSuccess()) {
                        sendData(ctx); // 递归发送，直到不可写
                    }
                });
                System.out.println("Data sent. Writable: " + ctx.channel().isWritable());
            } else {
                System.out.println("High watermark reached! Channel unwritable.");
                // 延迟1秒后重试
                ctx.channel().eventLoop().schedule(() -> sendData(ctx), 1, TimeUnit.SECONDS);
            }
        }

        @Override
        public void channelWritabilityChanged(ChannelHandlerContext ctx) {
            // 水位线变化时触发
            System.out.println("Writability changed: " + ctx.channel().isWritable());
        }
    }
}
