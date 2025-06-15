package cn.xilio.netty.demo11_udp.nat;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class UDPProxyServer {
    private final UDPProxyConfig config;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel udpChannel; // UDP 监听通道
    private Channel controlChannel; // TCP 控制通道
    private volatile Channel workConn; // 当前工作连接
    private final ConcurrentLinkedQueue<UDPPacket> sendQueue = new ConcurrentLinkedQueue<>(); // 待发送数据包队列
    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    public UDPProxyServer(UDPProxyConfig config) {
        this.config = config;
    }

    public String run() throws Exception {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        // 启动 UDP 监听
        Bootstrap udpBootstrap = new Bootstrap();
        udpBootstrap.group(workerGroup)
                .channel(NioDatagramChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ch.pipeline().addLast(new UDPHandler());
                    }
                });
        udpChannel = udpBootstrap.bind(config.getProxyBindAddr(), config.getRemotePort()).sync().channel();
        System.out.println("UDP proxy listening on " + config.getProxyBindAddr() + ":" + config.getRemotePort());

        // 启动 TCP 控制监听
        ServerBootstrap tcpBootstrap = new ServerBootstrap();
        tcpBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ch.pipeline().addLast(
                                new StringDecoder(),
                                new StringEncoder(),
                                new WorkConnHandler()
                        );
                    }
                });
        controlChannel = tcpBootstrap.bind(config.getControlPort()).sync().channel();
        System.out.println("TCP control listening on " + config.getProxyBindAddr() + ":" + config.getControlPort());

        // 启动工作连接轮询线程
        new Thread(this::pollWorkConn).start();

        return config.getProxyBindAddr() + ":" + config.getRemotePort();
    }

    private void pollWorkConn() {
        while (!isClosed.get()) {
            if (workConn == null || !workConn.isActive()) {
                System.out.println("Waiting for new work connection...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    public void close() {
        if (isClosed.compareAndSet(false, true)) {
            if (workConn != null) {
                workConn.close();
                System.out.println("Work connection closed");
            }
            if (udpChannel != null) {
                udpChannel.close();
                System.out.println("UDP channel closed");
            }
            if (controlChannel != null) {
                controlChannel.close();
                System.out.println("Control channel closed");
            }
            if (workerGroup != null) {
                workerGroup.shutdownGracefully();
                System.out.println("Worker group shutdown");
            }
            if (bossGroup != null) {
                bossGroup.shutdownGracefully();
                System.out.println("Boss group shutdown");
            }
            System.out.println("UDP proxy server closed");
        }
    }

    // 处理 UDP 数据包
    private class UDPHandler extends SimpleChannelInboundHandler<DatagramPacket> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {
            if (workConn == null || !workConn.isActive()) {
                System.out.println("No active work connection, discarding UDP packet from " + packet.sender());
                return;
            }

            ByteBuf buf = packet.content();
            byte[] content = new byte[buf.readableBytes()];
            buf.readBytes(content);
            String remoteAddr = packet.sender().getAddress().getHostAddress() + ":" + packet.sender().getPort();
            UDPPacket udpPacket = new UDPPacket(remoteAddr, content);
            sendQueue.offer(udpPacket);
            System.out.println("Received UDP packet from " + remoteAddr + ", forwarding to workConn");

            // 通过 workConn 发送
            workConn.writeAndFlush(buf).addListener(future -> {
                if (future.isSuccess()) {
                    System.out.println("Sent UDP packet to workConn");
                } else {
                    System.out.println("Failed to send UDP packet to workConn: " + future.cause());
                }
            });
        }
    }

    // 处理 workConn 数据
    private class WorkConnHandler extends SimpleChannelInboundHandler<ByteBuf> {
        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            if (workConn != null && workConn.isActive()) {
                workConn.close();
                System.out.println("Closed old work connection");
            }
            workConn = ctx.channel();
            System.out.println("New work connection established from " + ctx.channel().remoteAddress());
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
            UDPPacket packet = UDPPacket.decode(msg);
            String[] addrParts = packet.getRemoteAddr().split(":");
            String host = addrParts[0];
            int port = Integer.parseInt(addrParts[1]);
            InetSocketAddress recipient = new InetSocketAddress(host, port);
            ByteBuf buf = Unpooled.wrappedBuffer(packet.getContent());
            System.out.println("Received UDPPacket from workConn, sending to " + recipient);
            udpChannel.writeAndFlush(new DatagramPacket(buf, recipient)).addListener(future -> {
                if (future.isSuccess()) {
                    System.out.println("Sent UDP packet to " + recipient);
                } else {
                    System.out.println("Failed to send UDP packet to " + recipient + ": " + future.cause());
                }
            });
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            if (workConn == ctx.channel()) {
                workConn = null;
                System.out.println("Work connection closed");
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            System.out.println("WorkConn error: " + cause.getMessage());
            ctx.close();
        }
    }

    public static void main(String[] args) throws Exception {
        UDPProxyConfig config = new UDPProxyConfig();
        UDPProxyServer server = new UDPProxyServer(config);
        server.run();
        Runtime.getRuntime().addShutdownHook(new Thread(server::close));
    }
}
