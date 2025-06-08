package cn.xilio.netty.demo9;


import com.google.protobuf.ByteString;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ServerHandler extends SimpleChannelInboundHandler<TunnelMessage.Message> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TunnelMessage.Message msg) throws Exception {
        TunnelMessage.Message.Type type = msg.getType();
        if (type == TunnelMessage.Message.Type.AUTH) {
            // 认证
             ctx.writeAndFlush(TunnelMessage.Message.newBuilder()
                     .setType(TunnelMessage.Message.Type.TRANSFER)
                     .setSerialNumber(msg.getSerialNumber())
                     .setUri(msg.getUri())
                     .setData(ByteString.copyFrom("ok".getBytes())).build());
        }
    }
}
