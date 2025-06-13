package cn.xilio.netty.demo17_leak;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.CharsetUtil;
import io.netty.util.ResourceLeakDetector;

import java.util.ArrayList;
import java.util.List;

public class MemoryLeakExample {
    // 静态列表，用于模拟长生命周期对象持有ByteBuf引用
    private static final List<ByteBuf> LEAK_LIST = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException {
        // 配置内存泄漏检测
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .childHandler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 public void initChannel(SocketChannel ch) {
                     ch.pipeline().addLast(new LeakyHandler());
                 }
             })
             .option(ChannelOption.SO_BACKLOG, 128)
             .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = b.bind(8080).sync();
            System.out.println("服务器启动，端口8080");

            // 模拟客户端请求
            simulateClientRequests();

            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    // 模拟客户端请求
    private static void simulateClientRequests() {
        // 场景1：直接分配ByteBuf但忘记释放
        ByteBuf leakedBuf1 = Unpooled.directBuffer(1024);
        leakedBuf1.writeBytes("泄漏的内存1".getBytes(CharsetUtil.UTF_8));
        // 故意不调用release()

        // 场景2：添加到静态列表导致泄漏
        ByteBuf leakedBuf2 = Unpooled.directBuffer(1024);
        leakedBuf2.writeBytes("泄漏的内存2".getBytes(CharsetUtil.UTF_8));
        LEAK_LIST.add(leakedBuf2);

        // 场景3：在Handler中泄漏(见下面的LeakyHandler)
    }

    // 泄漏的ChannelHandler
    static class LeakyHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            ByteBuf buf = (ByteBuf) msg;
            try {
                // 读取数据但不释放
                System.out.println("收到消息: " + buf.toString(CharsetUtil.UTF_8));

                // 场景3：在Handler中处理消息但不释放
                ByteBuf response = ctx.alloc().buffer(1024);
                response.writeBytes("响应消息".getBytes(CharsetUtil.UTF_8));
                ctx.writeAndFlush(response);
                // 注意：这里response会被Netty自动释放，但输入的msg(buf)需要手动释放
            } finally {
               //  buf.release(); //如果不释放会发生内存泄漏

            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }
}
