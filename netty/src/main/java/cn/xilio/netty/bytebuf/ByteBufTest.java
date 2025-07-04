package cn.xilio.netty.bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ByteBufTest {
    public static void main(String[] args) {
        ByteBuf buffer = Unpooled.buffer(256);
        buffer.writeInt(42);  // 写入整数
        buffer.writeFloat(3.14f);  // 写入浮点数
        buffer.writeDouble(32.6D);  // 写入字节数组

        int readInt = buffer.readInt();
        double readDouble = buffer.readDouble();
        System.out.println(readInt);
        System.out.println(readDouble);
    }
}
