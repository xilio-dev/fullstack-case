package cn.xilio.netty.udp.weather;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

public class WeatherClient {

    private final String host;
    private final int port;

    public WeatherClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start(String city) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new SimpleChannelInboundHandler<DatagramPacket>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
                            String response = packet.content().toString(CharsetUtil.UTF_8);
                            System.out.println("收到天气信息: " + response);
                        }
                    });
            //0表示由【操作系统】随机生成一个端口分配给客户端
            Channel channel = bootstrap.bind(0).sync().channel();

            // 修复代码：使用 channel.alloc() 分配缓冲区
            channel.writeAndFlush(new DatagramPacket(
                    channel.alloc().buffer().writeBytes(city.getBytes(CharsetUtil.UTF_8)),
                    new InetSocketAddress(host, port)));

            // 等待响应
            channel.closeFuture().await(5000);
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws InterruptedException {
         String city = "Shanghai";
        new WeatherClient("127.0.0.1", 8080).start(city);
    }
}
