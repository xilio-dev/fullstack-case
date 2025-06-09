package cn.xilio.netty.demo12_retry.example2;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
/**
 * 高级版本 断线重连 指数退避算法、随机抖动因子、备用服务器切换
 */
public class AdvancedReconnectClient {

    public interface ReconnectListener {
        void onReconnectAttempt(int attempt, long delay);
        void onReconnectSuccess();
        void onReconnectFailure();
    }

    private final String host;
    private final int port;
    private final int maxRetries;
    private final long maxDelaySec;
    private final long initialDelaySec;

    private Bootstrap bootstrap;
    private EventLoopGroup group;
    private AtomicInteger retryCount = new AtomicInteger(0);
    private List<ReconnectListener> listeners = new CopyOnWriteArrayList<>();
    private List<String> backupHosts;
    private int currentHostIndex = 0;

    public AdvancedReconnectClient(String host, int port, int maxRetries,
                                 long maxDelaySec, long initialDelaySec,
                                 List<String> backupHosts) {
        this.host = host;
        this.port = port;
        this.maxRetries = maxRetries;
        this.maxDelaySec = maxDelaySec;
        this.initialDelaySec = initialDelaySec;
        this.backupHosts = backupHosts;
        initBootstrap();
    }

    private void initBootstrap() {
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new IdleStateHandler(0, 0, 30));
                        pipeline.addLast(new AdvancedReconnectHandler(AdvancedReconnectClient.this));
                    }
                });
    }

    public void connect() {
        String currentHost = getCurrentHost();
        ChannelFuture future = bootstrap.connect(currentHost, port);
        future.addListener((ChannelFutureListener) f -> {
            if (!f.isSuccess()) {
                System.err.println("连接" + currentHost + "失败，准备重连...");
                notifyReconnectAttempt(retryCount.get() + 1, calculateDelay());
                scheduleReconnect();
            } else {
                System.out.println("连接" + currentHost + "成功!");
                retryCount.set(0);
                notifyReconnectSuccess();
            }
        });
    }

    private String getCurrentHost() {
        if (currentHostIndex == 0) {
            return host;
        }
        return backupHosts.get((currentHostIndex - 1) % backupHosts.size());
    }

    private void switchToNextHost() {
        currentHostIndex = (currentHostIndex + 1) % (backupHosts.size() + 1);
        System.out.println("切换到备用服务器: " + getCurrentHost());
    }

    private long calculateDelay() {
        int retries = retryCount.get();
        if (retries == 0) {
            return initialDelaySec;
        }

        // 指数退避 + 随机抖动(±30%)
        long delay = Math.min((1L << retries), maxDelaySec);
        long jitter = (long)(delay * 0.3 * (Math.random() * 2 - 1));
        return Math.min(delay + jitter, maxDelaySec);
    }

    private void scheduleReconnect() {
        if (retryCount.get() >= maxRetries) {
            System.err.println("达到最大重试次数，停止重连");
            notifyReconnectFailure();
            group.shutdownGracefully();
            return;
        }

        retryCount.incrementAndGet();
        long delay = calculateDelay();

        System.out.printf("第%d次重连将在%d秒后执行...%n", retryCount.get(), delay);

        group.schedule(() -> {
            System.out.println("执行重连...");
            if (retryCount.get() % 3 == 0) { // 每失败3次切换一次服务器
                switchToNextHost();
            }
            connect();
        }, delay, TimeUnit.SECONDS);
    }

    public void addReconnectListener(ReconnectListener listener) {
        listeners.add(listener);
    }

    private void notifyReconnectAttempt(int attempt, long delay) {
        listeners.forEach(l -> l.onReconnectAttempt(attempt, delay));
    }

    private void notifyReconnectSuccess() {
        listeners.forEach(ReconnectListener::onReconnectSuccess);
    }

    private void notifyReconnectFailure() {
        listeners.forEach(ReconnectListener::onReconnectFailure);
    }

    public static void main(String[] args) {
        AdvancedReconnectClient client = new AdvancedReconnectClient(
                "localhost", 8080,
                15, 60, 2,
                List.of("localhost", "127.0.0.1"));

        client.addReconnectListener(new ReconnectListener() {
            @Override
            public void onReconnectAttempt(int attempt, long delay) {
                System.out.printf("[监听器] 第%d次重连尝试，延迟%.1f秒%n", attempt, (float)delay);
            }

            @Override
            public void onReconnectSuccess() {
                System.out.println("[监听器] 重连成功");
            }

            @Override
            public void onReconnectFailure() {
                System.out.println("[监听器] 重连失败");
            }
        });

        client.connect();
    }
}

class AdvancedReconnectHandler extends ChannelInboundHandlerAdapter {
    private final AdvancedReconnectClient client;

    public AdvancedReconnectHandler(AdvancedReconnectClient client) {
        this.client = client;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("连接断开，准备重连...");
        ctx.channel().eventLoop().schedule(() -> {
            client.connect();
        }, 1, TimeUnit.SECONDS);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            System.out.println("检测到连接空闲，主动关闭并重连...");
            ctx.channel().close().addListener(future -> client.connect());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
