package cn.xilio.netty.traffic;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;
//出站 入站流量监控统计
// 自定义流量监控处理器
class TrafficMonitorHandler extends ChannelDuplexHandler {
    private final AtomicLong inboundTraffic = new AtomicLong(0); // 入站流量
    private final AtomicLong outboundTraffic = new AtomicLong(0); // 出站流量

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            ByteBuf byteBuf = (ByteBuf) msg;
            int readableBytes = byteBuf.readableBytes();
            inboundTraffic.addAndGet(readableBytes);
            System.out.println("入站流量: " + readableBytes + " 字节 (总计: " + inboundTraffic.get() + " 字节)");
        }
        super.channelRead(ctx, msg); // 继续传递到下一个入站处理器
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof ByteBuf) {
            ByteBuf byteBuf = (ByteBuf) msg;
            int readableBytes = byteBuf.readableBytes();
            outboundTraffic.addAndGet(readableBytes);
            System.out.println("出站流量: " + readableBytes + " 字节 (总计: " + outboundTraffic.get() + " 字节)");
        }
        super.write(ctx, msg, promise); // 继续传递到下一个出站处理器
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("连接关闭 - 入站总流量: " + inboundTraffic.get() + " 字节, 出站总流量: " + outboundTraffic.get() + " 字节");
        super.channelInactive(ctx);
    }
}

// 服务端
class TrafficMonitorServer {
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new TrafficMonitorHandler()); // 添加流量监控处理器
                        }
                    });

            ChannelFuture future = serverBootstrap.bind(8090).sync();
            System.out.println("流量监控服务端已启动，监听端口 8080...");
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}

// 客户端
class TrafficMonitorClient {
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new TrafficMonitorHandler()); // 添加流量监控处理器
                        }
                    });

            Channel channel = bootstrap.connect("127.0.0.1", 8090).sync().channel();
            System.out.println("已连接到服务端...");

            // 模拟发送多次数据
            for (int i = 0; i < 50; i++) {
                String message = "这是我发送的消息，我非常喜欢发送消息给你 " + (i + 1);
                ByteBuf buffer = Unpooled.copiedBuffer(message, StandardCharsets.UTF_8);
                channel.writeAndFlush(buffer);
                Thread.sleep(1000); // 每秒发送一次
            }

            channel.closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}
