package cn.xilio.netty.demo1;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class DiscardServerHandler extends ChannelInboundHandlerAdapter { // (1)

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) { // (2)

     /*   ByteBuf in = (ByteBuf) msg;
        try {
            while (in.isReadable()) { // (1)
                System.out.print((char) in.readByte());
                System.out.flush();
            }
        } finally {
            ReferenceCountUtil.release(msg); // (2)
        }*/
//        ctx.write(msg); // (1)
//        ctx.flush(); // (2)
        ctx.channel().eventLoop().execute(new Runnable() {
            @Override
            public void run() {
               // Thread.sleep(1 * 10000);//1秒后执行
                System.out.println("task 1");
            }
        });
        ctx.channel().eventLoop().execute(new Runnable() {
            @Override
            public void run() {
               // Thread.sleep(2 * 10000);
                System.out.println("task 2"); //30秒后执行
            }
        });
        ctx.writeAndFlush(msg);
    }
}

