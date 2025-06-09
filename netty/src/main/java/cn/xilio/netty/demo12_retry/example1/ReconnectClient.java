package cn.xilio.netty.demo12_retry.example1;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基本版本 断线重连 指数退避算法
 */
public class ReconnectClient {
    private final String host;
    private final int port;
    //最大重试次数 超过以后优雅关闭workerGroup
    private final int maxRetries;
    //最大延迟时间 如果超过了则取maxDelaySec为最大延迟时间
    private final long maxDelaySec;

    private Bootstrap bootstrap;
    private EventLoopGroup workerGroup;
    //当前重试次数
    private AtomicInteger retryCount = new AtomicInteger(0);

    public ReconnectClient(String host, int port, int maxRetries, long maxDelaySec) {
        this.host = host;
        this.port = port;
        this.maxRetries = maxRetries;
        this.maxDelaySec = maxDelaySec;
        initBootstrap();
    }

    private void initBootstrap() {
        workerGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(workerGroup).channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new IdleStateHandler(0, 0, 30));
                        pipeline.addLast(new ReconnectHandler(ReconnectClient.this));
                    }
                });
    }

    public void connect() {
        ChannelFuture future = bootstrap.connect(host, port);
        future.addListener((ChannelFutureListener) f -> {
            if (!f.isSuccess()) {
                System.err.println("连接失败，准备重连...");
                scheduleReconnect();
            } else {
                System.out.println("连接成功!");
                retryCount.set(0); // 重置重试计数器
            }
        });
    }

    private void scheduleReconnect() {
        if (retryCount.get() >= maxRetries) {
            System.err.println("达到最大重试次数，停止重连");
            workerGroup.shutdownGracefully();
            return;
        }

        // 计算退避时间 (2^n秒，最大不超过maxDelaySec)
        int retries = retryCount.getAndIncrement();
        //指数计算，如果超过了最大延迟时间，则取最大延迟时间
        long delay = Math.min((1L << retries), maxDelaySec);
        System.out.printf("第%d次重连将在%d秒后执行...%n", retries + 1, delay);
        // 调度重连任务
        workerGroup.schedule(() -> {
            System.out.println("执行重连...");
            connect();
        }, delay, TimeUnit.SECONDS);
    }

    public static void main(String[] args) {
        ReconnectClient client = new ReconnectClient("127.0.0.1", 8080, 10, 60);
        client.connect();
    }
}
//可选，根据业务选择
class ReconnectHandler extends ChannelInboundHandlerAdapter {
    private final ReconnectClient client;

    public ReconnectHandler(ReconnectClient client) {
        this.client = client;
    }
    //服务端断开连接的时候尝试重连接，但是需要结合业务，有的场景不可以在此处处理重新连接！！！
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("连接断开，准备重连...");
        ctx.channel().eventLoop().schedule(() -> {
            client.connect();
        }, 1, TimeUnit.SECONDS); // 立即重连
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
