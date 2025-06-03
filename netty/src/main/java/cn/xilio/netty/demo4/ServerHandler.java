package cn.xilio.netty.demo4;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ServerHandler extends SimpleChannelInboundHandler<MessageProto.DataMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageProto.DataMessage msg) {
        System.out.printf("收到消息: ID=%d 内容=%s 标签=%s\n",
            msg.getId(), msg.getContent(), msg.getTagsList());

        // 构造响应
        MessageProto.DataMessage response = MessageProto.DataMessage.newBuilder()
            .setId(msg.getId() * 2)
            .setContent("响应-" + msg.getContent())
            .addAllTags(msg.getTagsList())
            .build();

        ctx.writeAndFlush(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
