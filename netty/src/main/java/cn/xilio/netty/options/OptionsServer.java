package cn.xilio.netty.options;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;


public class OptionsServer {

    private int port;

    public OptionsServer(int port) {
        this.port = port;
    }

    public void run() throws Exception {

        EventLoopGroup bossGroup = new NioEventLoopGroup(1); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // (3)
                    .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new StringDecoder());
                            ch.pipeline().addLast(new StringEncoder());
                            ch.pipeline().addLast(new SimpleChannelInboundHandler<String>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
                                    if (ctx.channel().isWritable()) {
                                        System.out.println("Channel is writable");
                                        ctx.channel().writeAndFlush(msg);
                                    }else {
                                        System.out.println("Channel is not writable");
                                    }

                                }
                            });
                        }
                    })
                    //Channel.isWritable() 是 Netty 中用于流量控制的核心方法，其作用是判断当前 Channel 的写缓冲区（ChannelOutboundBuffer）是否可接受新的数据写入，以避免因缓冲区积压导致内存溢出（OOM）或性能下降。
                    //高水位线（High Water Mark）：默认 64KB，缓冲区大小超过此值时，isWritable() 返回 false。
                    //低水位线（Low Water Mark）：默认 32KB，缓冲区低于此值时，isWritable() 恢复为 true。
                    .option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(1 , 2))
                    .option(ChannelOption.TCP_NODELAY, false) //禁用Nagle算法（低延迟场景）
                    .option(ChannelOption.SO_BACKLOG, 128)  //等待队列大小  处理能力不足时连接进入等待队列      // (5)
                    .childOption(ChannelOption.SO_KEEPALIVE, true); //

            ChannelFuture f = b.bind(port).sync(); // (7)
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 8200;
        new OptionsServer(port).run();
    }
}
