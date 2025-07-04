package cn.xilio.netty.bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ByteBufExample {
    public static void main(String[] args) {
        // 创建一个非池化的 ByteBuf
        ByteBuf buffer = Unpooled.buffer(256);

        System.out.println("初始容量: " + buffer.capacity());

        // 写入数据到 ByteBuf
        buffer.writeInt(42);  // 写入整数
        buffer.writeFloat(3.14f);  // 写入浮点数
        buffer.writeBytes(new byte[]{1, 2, 3, 4});  // 写入字节数组

        System.out.println("写入后的容量: " + buffer.capacity());
        System.out.println("写入索引: " + buffer.writerIndex());

        // 读取数据
        int intValue = buffer.readInt();
        float floatValue = buffer.readFloat();
        byte[] byteArray = new byte[4];
        buffer.readBytes(byteArray);


        System.out.println("读取的整数: " + intValue);
        System.out.println("读取的浮点数: " + floatValue);
        System.out.print("读取的字节数组: ");
        for (byte b : byteArray) {
            System.out.print(b + " ");
        }
        System.out.println();

        // 切片操作（零拷贝）
        ByteBuf sliceBuffer = buffer.slice(0, buffer.readableBytes());
        System.out.println("切片后的容量: " + sliceBuffer.capacity());

        // 修改原始缓冲区的数据会影响切片
        sliceBuffer.writeByte(99);
        System.out.println("原始缓冲区的读索引: " + buffer.readerIndex());
        System.out.println("切片缓冲区的写索引: " + sliceBuffer.writerIndex());

        // 释放资源
        buffer.release();
        sliceBuffer.release();
    }
}
