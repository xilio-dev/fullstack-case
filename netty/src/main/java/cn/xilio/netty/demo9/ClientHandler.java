package cn.xilio.netty.demo9;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ClientHandler extends SimpleChannelInboundHandler<TunnelMessage.Message> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TunnelMessage.Message msg) throws Exception {
        System.out.println(msg);
    }
}
