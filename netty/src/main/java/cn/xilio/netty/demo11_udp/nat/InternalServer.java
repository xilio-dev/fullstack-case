package cn.xilio.netty.demo11_udp.nat;

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

public class InternalServer {
    private final String host;
    private final int port;
    private EventLoopGroup group;
    private Channel channel;
    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    public InternalServer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() throws Exception {
        group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioDatagramChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ch.pipeline().addLast(new InternalServerHandler());
                    }
                });

        channel = bootstrap.bind(host, port).sync().channel();
        System.out.println("Internal server started, listening on " + host + ":" + port);
    }

    public void close() {
        if (isClosed.compareAndSet(false, true)) {
            if (channel != null) {
                channel.close();
                System.out.println("Internal server channel closed");
            }
            if (group != null) {
                group.shutdownGracefully();
                System.out.println("Internal server event loop group shutdown");
            }
            System.out.println("Internal server closed");
        }
    }

    private class InternalServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {
            ByteBuf buf = packet.content();
            String message = buf.toString(StandardCharsets.UTF_8);
            System.out.println("Received request from " + packet.sender() + ": " + message);

            // 发送响应
            String response = "Response";
            ByteBuf responseBuf = Unpooled.copiedBuffer(response, StandardCharsets.UTF_8);
            DatagramPacket responsePacket = new DatagramPacket(responseBuf, packet.sender());
            ctx.writeAndFlush(responsePacket).addListener(future -> {
                if (future.isSuccess()) {
                    System.out.println("Sent response to " + packet.sender() + ": " + response);
                } else {
                    System.out.println("Failed to send response: " + future.cause().getMessage());
                }
            });
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            System.out.println("Internal server error: " + cause.getMessage());
            ctx.close();
        }
    }

    public static void main(String[] args) throws Exception {
        InternalServer server = new InternalServer("127.0.0.1", 8000);
        server.run();
        Runtime.getRuntime().addShutdownHook(new Thread(server::close));
    }
}
