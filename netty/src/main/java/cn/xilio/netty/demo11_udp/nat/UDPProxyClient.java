package cn.xilio.netty.demo11_udp.nat;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class UDPProxyClient {
    private final UDPProxyClientConfig config;
    private EventLoopGroup group;
    private Channel udpChannel; // 本地 UDP 监听通道
    private volatile Channel workConn; // TCP 工作连接
    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    public UDPProxyClient(UDPProxyClientConfig config) {
        this.config = config;
    }

    public void run() throws Exception {
        group = new NioEventLoopGroup();

        // 启动本地 UDP 监听
        Bootstrap udpBootstrap = new Bootstrap();
        udpBootstrap.group(group)
                .channel(NioDatagramChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ch.pipeline().addLast(new UDPHandler());
                    }
                });
        udpChannel = udpBootstrap.bind(config.getLocalAddr(), config.getLocalPort()).sync().channel();
        System.out.println("Client UDP listening on " + config.getLocalAddr() + ":" + config.getLocalPort());

        // 连接服务器
        connectWorkConn();
    }

    private void connectWorkConn() {
        Bootstrap tcpBootstrap = new Bootstrap();
        tcpBootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ch.pipeline().addLast(
                                new StringDecoder(),
                                new StringEncoder(),
                                new WorkConnHandler()
                        );
                    }
                });
        tcpBootstrap.connect(config.getServerAddr(), config.getServerControlPort()).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                workConn = future.channel();
                System.out.println("Connected to server at " + config.getServerAddr() + ":" + config.getServerControlPort());
            } else {
                System.out.println("Failed to connect to server, retrying in 1s...");
                if (!isClosed.get()) {
                    group.schedule(this::connectWorkConn, 1, TimeUnit.SECONDS);
                }
            }
        });
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
            if (group != null) {
                group.shutdownGracefully();
                System.out.println("Event loop group shutdown");
            }
            System.out.println("UDP proxy client closed");
        }
    }

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
            System.out.println("Received UDP packet from " + remoteAddr + ", forwarding to workConn");
            workConn.writeAndFlush(udpPacket.encode()).addListener(future -> {
                if (future.isSuccess()) {
                    System.out.println("Sent UDP packet to workConn");
                } else {
                    System.out.println("Failed to send UDP packet to workConn: " + future.cause());
                }
            });
        }
    }

    private class WorkConnHandler extends SimpleChannelInboundHandler<ByteBuf> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
            if (msg.readableBytes() == 8 && msg.getInt(4) == 0) {
                msg.skipBytes(8);
                System.out.println("Received heartbeat from server");
                return;
            }

            byte[] rawData = new byte[msg.readableBytes()];
            msg.getBytes(msg.readerIndex(), rawData);
            System.out.println("Received raw data from workConn, length: " + rawData.length);

            try {
                UDPPacket packet = UDPPacket.decode(msg);
                InetSocketAddress recipient = new InetSocketAddress("127.0.0.1", 8000);
                ByteBuf buf = Unpooled.wrappedBuffer(packet.getContent());
                System.out.println("Received UDPPacket from workConn, sending to " + recipient + ", data length: " + packet.getContent().length);
                udpChannel.writeAndFlush(new DatagramPacket(buf, recipient)).addListener(future -> {
                    if (future.isSuccess()) {
                        System.out.println("Sent UDP packet to " + recipient + " successfully");
                    } else {
                        System.out.println("Failed to send UDP packet to " + recipient + ": " + future.cause().getMessage());
                    }
                });
            } catch (Exception e) {
                System.out.println("Failed to decode UDPPacket: " + e.getMessage());
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            if (workConn == ctx.channel()) {
                workConn = null;
                System.out.println("Work connection closed, reconnecting...");
                if (!isClosed.get()) {
                    connectWorkConn();
                }
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            System.out.println("WorkConn error: " + cause.getMessage());
            ctx.close();
        }
    }

    public static void main(String[] args) throws Exception {
        UDPProxyClientConfig config = new UDPProxyClientConfig();
        UDPProxyClient client = new UDPProxyClient(config);
        client.run();
        Runtime.getRuntime().addShutdownHook(new Thread(client::close));
    }
}
