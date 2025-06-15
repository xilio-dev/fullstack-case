package cn.xilio.netty.demo11_udp.weather;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;

import java.util.HashMap;
import java.util.Map;

public class WeatherServer {

    private final int port;

    // 模拟的天气数据
    private static final Map<String, String> WEATHER_DATA = new HashMap<>();

    static {
        WEATHER_DATA.put("Beijing", "晴天，24°C");
        WEATHER_DATA.put("Shanghai", "多云，28°C");
        WEATHER_DATA.put("Guangzhou", "阵雨，30°C");
        WEATHER_DATA.put("Shenzhen", "雷阵雨，31°C");
    }

    public WeatherServer(int port) {
        this.port = port;
    }

    public void start() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)//广播
                    .handler(new SimpleChannelInboundHandler<DatagramPacket>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
                            String city = packet.content().toString(CharsetUtil.UTF_8).trim();
                            System.out.println("收到查询请求: " + city);

                            String weatherInfo = WEATHER_DATA.getOrDefault(city, "城市天气数据未找到");


//                            DatagramPacket response = new DatagramPacket(
//                                    ctx.alloc().buffer().writeBytes(weatherInfo.getBytes(CharsetUtil.UTF_8)),
//                                    packet.sender());

                            // 使用堆外内存构造响应数据
                            ByteBuf responseContent = Unpooled.directBuffer();
                            responseContent.writeBytes(weatherInfo.getBytes(CharsetUtil.UTF_8));

                            DatagramPacket response = new DatagramPacket(
                                    responseContent,
                                    packet.sender());

                            ctx.writeAndFlush(response);
                        }
                    });

            ChannelFuture future = bootstrap.bind(port).sync();
            System.out.println("天气查询服务器已启动，端口: " + port);
            future.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new WeatherServer(8080).start();
    }
}
