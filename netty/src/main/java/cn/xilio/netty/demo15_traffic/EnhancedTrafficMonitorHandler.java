package cn.xilio.netty.demo15_traffic;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.buffer.ByteBuf;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

// 自定义流量监控与限制处理器 限制入站和出站流量 多客户端支持 不同的客户端隔离
class EnhancedTrafficMonitorHandler extends ChannelDuplexHandler {
    private static final long MAX_TRAFFIC_PER_CLIENT =  1024; // 每个客户端最大流量限制（1 KB）
    private final AtomicLong inboundTraffic = new AtomicLong(0); // 当前连接的入站流量
    private final AtomicLong outboundTraffic = new AtomicLong(0); // 当前连接的出站流量
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
            inboundTraffic.addAndGet(readableBytes); // 累加当前连接的入站流量

            // 获取客户端地址并更新全局统计
            SocketAddress clientAddress = ctx.channel().remoteAddress();
            ClientTrafficStats stats = clientTrafficMap.get(clientAddress);
            stats.addInboundTraffic(readableBytes);

            System.out.println("客户端 " + clientAddress + " 入站流量: " + readableBytes + " 字节 (总计: " + stats.getInboundTraffic() + " 字节)");

            // 检查流量限制
            if (stats.getTotalTraffic() > MAX_TRAFFIC_PER_CLIENT) {
                System.out.println("客户端 " + clientAddress + " 流量超出限制，关闭连接...");
                ctx.close();
                return;
            }
        }
        super.channelRead(ctx, msg); // 继续传递到下一个入站处理器
    }
//写出去的流量
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof ByteBuf) {
            ByteBuf byteBuf = (ByteBuf) msg;
            int readableBytes = byteBuf.readableBytes();
            outboundTraffic.addAndGet(readableBytes); // 累加当前连接的出站流量

            // 获取客户端地址并更新全局统计
            SocketAddress clientAddress = ctx.channel().remoteAddress();
            ClientTrafficStats stats = clientTrafficMap.get(clientAddress);
            stats.addOutboundTraffic(readableBytes);

            System.out.println("客户端 " + clientAddress + " 出站流量: " + readableBytes + " 字节 (总计: " + stats.getOutboundTraffic() + " 字节)");

            // 检查流量限制
            if (stats.getTotalTraffic() > MAX_TRAFFIC_PER_CLIENT) {
                System.out.println("客户端 " + clientAddress + " 流量超出限制，关闭连接...");
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

// 客户端流量统计类 多客户端隔离
class ClientTrafficStats {
    private final AtomicLong inboundTraffic = new AtomicLong(0); // 入站流量
    private final AtomicLong outboundTraffic = new AtomicLong(0); // 出站流量

    public void addInboundTraffic(long bytes) {
        inboundTraffic.addAndGet(bytes);
    }

    public void addOutboundTraffic(long bytes) {
        outboundTraffic.addAndGet(bytes);
    }

    public long getInboundTraffic() {
        return inboundTraffic.get();
    }

    public long getOutboundTraffic() {
        return outboundTraffic.get();
    }

    public long getTotalTraffic() {
        return inboundTraffic.get() + outboundTraffic.get();
    }
}

// 流量监控服务端
class EnhancedTrafficMonitorServer {
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
                            pipeline.addLast(new EnhancedTrafficMonitorHandler()); // 添加流量监控处理器
                        }
                    });

            ChannelFuture future = serverBootstrap.bind(8090).sync();
            System.out.println("增强版流量监控服务端已启动，监听端口 8090...");
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
