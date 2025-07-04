package cn.xilio.netty.udp.nat;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

public class ExternalClient {
    private final String serverHost;
    private final int serverPort;
    private EventLoopGroup group;
    private Channel channel;
    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    public ExternalClient(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    public void run() throws Exception {
        group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioDatagramChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ch.pipeline().addLast(new ExternalClientHandler());
                    }
                });

        channel = bootstrap.bind(0).sync().channel(); // 绑定随机端口
        System.out.println("External client started, local address: " + channel.localAddress());

        // 发送测试数据包
        sendTestPacket();
    }

    private void sendTestPacket() {
        String message = "Hello";
        ByteBuf buf = Unpooled.copiedBuffer(message, StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(buf, new InetSocketAddress(serverHost, serverPort));
        channel.writeAndFlush(packet).addListener(future -> {
            if (future.isSuccess()) {
                System.out.println("Sent test packet to " + serverHost + ":" + serverPort + ": " + message);
            } else {
                System.out.println("Failed to send test packet: " + future.cause().getMessage());
            }
        });
    }

    public void close() {
        if (isClosed.compareAndSet(false, true)) {
            if (channel != null) {
                channel.close();
                System.out.println("External client channel closed");
            }
            if (group != null) {
                group.shutdownGracefully();
                System.out.println("External client event loop group shutdown");
            }
            System.out.println("External client closed");
        }
    }

    private class ExternalClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {
            ByteBuf buf = packet.content();
            String message = buf.toString(StandardCharsets.UTF_8);
            System.out.println("Received response from " + packet.sender() + ": " + message);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            System.out.println("External client error: " + cause.getMessage());
            ctx.close();
        }
    }

    public static void main(String[] args) throws Exception {
        ExternalClient client = new ExternalClient("127.0.0.1", 7000);
        client.run();
        Runtime.getRuntime().addShutdownHook(new Thread(client::close));
        // 保持运行以接收响应
        Thread.sleep(60000); // 等待 60 秒
        client.close();
    }
}
