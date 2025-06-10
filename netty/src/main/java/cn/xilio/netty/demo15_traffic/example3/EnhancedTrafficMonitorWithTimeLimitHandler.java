package cn.xilio.netty.demo15_traffic.example3;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.buffer.ByteBuf;

import java.net.SocketAddress;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

// 自定义流量监控与分时间段限制处理器 每小时流量最大限制
class EnhancedTrafficMonitorWithTimeLimitHandler extends ChannelDuplexHandler {
    private static final long MAX_HOURLY_TRAFFIC_PER_CLIENT = 1024 * 1024; // 每小时最大流量限制（1 MB）
    private static final Map<SocketAddress, ClientTrafficStats> clientTrafficMap = new ConcurrentHashMap<>(); // 全部客户端流量统计

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        SocketAddress clientAddress = ctx.channel().remoteAddress();
        clientTrafficMap.putIfAbsent(clientAddress, new ClientTrafficStats()); // 初始化客户端统计
        System.out.println("客户端连接: " + clientAddress);
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            ByteBuf byteBuf = (ByteBuf) msg;
            int readableBytes = byteBuf.readableBytes();

            // 获取客户端地址并更新全局统计
            SocketAddress clientAddress = ctx.channel().remoteAddress();
            ClientTrafficStats stats = clientTrafficMap.get(clientAddress);
            stats.addInboundTraffic(readableBytes);

            System.out.println("客户端 " + clientAddress + " 入站流量: " + readableBytes + " 字节 (本小时总计: " + stats.getHourlyTraffic() + " 字节)");

            // 检查每小时的流量限制
            if (stats.getHourlyTraffic() > MAX_HOURLY_TRAFFIC_PER_CLIENT) {
                System.out.println("客户端 " + clientAddress + " 超出每小时流量限制，关闭连接...");
                ctx.close();
                return;
            }
        }
        super.channelRead(ctx, msg); // 继续传递到下一个入站处理器
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof ByteBuf) {
            ByteBuf byteBuf = (ByteBuf) msg;
            int readableBytes = byteBuf.readableBytes();

            // 获取客户端地址并更新全局统计
            SocketAddress clientAddress = ctx.channel().remoteAddress();
            ClientTrafficStats stats = clientTrafficMap.get(clientAddress);
            stats.addOutboundTraffic(readableBytes);

            System.out.println("客户端 " + clientAddress + " 出站流量: " + readableBytes + " 字节 (本小时总计: " + stats.getHourlyTraffic() + " 字节)");

            // 检查每小时的流量限制
            if (stats.getHourlyTraffic() > MAX_HOURLY_TRAFFIC_PER_CLIENT) {
                System.out.println("客户端 " + clientAddress + " 超出每小时流量限制，关闭连接...");
                ctx.close();
                return;
            }
        }
        super.write(ctx, msg, promise); // 继续传递到下一个出站处理器
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        SocketAddress clientAddress = ctx.channel().remoteAddress();
        ClientTrafficStats stats = clientTrafficMap.remove(clientAddress); // 移除断开连接的客户端统计
        System.out.println("客户端断开连接: " + clientAddress);
        System.out.println("客户端 " + clientAddress + " 总入站流量: " + stats.getInboundTraffic() + " 字节");
        System.out.println("客户端 " + clientAddress + " 总出站流量: " + stats.getOutboundTraffic() + " 字节");
        super.channelInactive(ctx);
    }
}

// 客户端流量统计类
class ClientTrafficStats {
    private final AtomicLong inboundTraffic = new AtomicLong(0); // 总入站流量
    private final AtomicLong outboundTraffic = new AtomicLong(0); // 总出站流量
    private final AtomicLong hourlyTraffic = new AtomicLong(0); // 每小时流量
    private volatile long currentHourStart = Instant.now().getEpochSecond() / 3600; // 当前小时起始时间

    public synchronized void addInboundTraffic(long bytes) {
        resetHourlyTrafficIfNeeded();
        inboundTraffic.addAndGet(bytes);
        hourlyTraffic.addAndGet(bytes);
    }

    public synchronized void addOutboundTraffic(long bytes) {
        resetHourlyTrafficIfNeeded();
        outboundTraffic.addAndGet(bytes);
        hourlyTraffic.addAndGet(bytes);
    }

    public long getInboundTraffic() {
        return inboundTraffic.get();
    }

    public long getOutboundTraffic() {
        return outboundTraffic.get();
    }

    public long getHourlyTraffic() {
        resetHourlyTrafficIfNeeded();
        return hourlyTraffic.get();
    }

    public long getTotalTraffic() {
        return inboundTraffic.get() + outboundTraffic.get();
    }

    // 如果跨小时，则重置每小时流量
    private void resetHourlyTrafficIfNeeded() {
        long currentHour = Instant.now().getEpochSecond() / 3600;
        if (currentHour != currentHourStart) {
            hourlyTraffic.set(0);
            currentHourStart = currentHour;
        }
    }
}

// 服务端
class EnhancedTrafficMonitorWithTimeLimitServer {
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
                            pipeline.addLast(new EnhancedTrafficMonitorWithTimeLimitHandler()); // 添加流量监控与限制器
                        }
                    });

            ChannelFuture future = serverBootstrap.bind(8120).sync();
            System.out.println("服务端已启动，监听端口 8120...");
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
