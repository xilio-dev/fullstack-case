package cn.xilio.netty.demo4;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ClientHandler extends SimpleChannelInboundHandler<MessageProto.DataMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageProto.DataMessage msg) {
        System.out.printf("收到响应: ID=%d 内容=%s\n", msg.getId(), msg.getContent());
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
