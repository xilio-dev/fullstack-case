package cn.xilio.netty.demo6;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

public class ChatServer {
    // ... 主类代码不变，修改 ChannelInitializer
    protected void initChannel(Channel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new IdleStateHandler(30, 0, 0)); // 30秒未读触发空闲
        pipeline.addLast(new StringDecoder());
        pipeline.addLast(new StringEncoder());
        pipeline.addLast(new ChatServerHandler());
    }

    static class ChatServerHandler extends SimpleChannelInboundHandler<String> {
        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent event = (IdleStateEvent) evt;
                if (event.state() == IdleState.READER_IDLE) {
                    ctx.writeAndFlush("Ping timeout, closing connection...");
                    ctx.close();
                }
            } else {
                super.userEventTriggered(ctx, evt);
            }
        }


        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {

        }

    }
}
