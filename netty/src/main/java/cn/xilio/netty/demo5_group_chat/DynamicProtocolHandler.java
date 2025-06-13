package cn.xilio.netty.demo5_group_chat;

import io.netty.channel.*;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class DynamicProtocolHandler extends SimpleChannelInboundHandler<String> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        if (msg.equals("/switchToProtobuf")) {
            // 动态修改 Pipeline，切换到 Protobuf 协议
            ChannelPipeline pipeline = ctx.pipeline();
            pipeline.remove(StringDecoder.class);
            pipeline.remove(StringEncoder.class);
            pipeline.remove(this); // 移除当前 Handler
            pipeline.addLast(new ProtobufDecoder( null));
            pipeline.addLast(new ProtobufEncoder());
            pipeline.addLast(new ProtobufHandler());
            ctx.writeAndFlush("Switched to Protobuf protocol!");
        } else {
            ctx.writeAndFlush("Echo: " + msg);
        }
    }
}

class ProtobufHandler extends SimpleChannelInboundHandler<Object> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 处理 Protobuf 消息
        ctx.writeAndFlush(msg);
    }
}
