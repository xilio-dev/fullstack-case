package cn.xilio.netty.demo10_http;

import com.google.gson.Gson;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.buffer.Unpooled;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class NettyHttpServer {
    private static final Gson gson = new Gson();

    public static void main(String[] args) throws Exception {
        // 创建 boss 和 worker 线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(1);

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer() {
                        @Override
                        protected void initChannel(Channel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            // 添加 HTTP 编解码器
                            pipeline.addLast(new HttpServerCodec());
                            // 聚合 HTTP 消息
                            pipeline.addLast(new HttpObjectAggregator(64 * 1024));
                            pipeline.addLast(new ChunkedWriteHandler());
                            // 自定义处理器
                            pipeline.addLast(new HttpRequestHandler());
                        }
                    });

            // 绑定端口
            ChannelFuture future = bootstrap.bind(8010).sync();
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
    static class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
            HttpMethod method = request.method();
            String uri = request.uri();
            HttpResponseStatus status = HttpResponseStatus.OK;
            String responseContent = "";
            try {
                if (method.equals(HttpMethod.GET) && uri.equals("/user/get")) {
                    ConcurrentMap<Object, Object> user = new ConcurrentHashMap<>();
                    user.put("id", 1);
                    user.put("name", "xilio");
                    responseContent = gson.toJson(user);
                }
            } catch (Exception e) {
                status = HttpResponseStatus.INTERNAL_SERVER_ERROR;
            }

            // 构建响应
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_0, status, Unpooled.copiedBuffer(responseContent, StandardCharsets.UTF_8));
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            ChannelFuture future = ctx.writeAndFlush(response);
            if (!HttpHeaders.isKeepAlive(request)) {
                future.addListener(ChannelFutureListener.CLOSE);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }
}
