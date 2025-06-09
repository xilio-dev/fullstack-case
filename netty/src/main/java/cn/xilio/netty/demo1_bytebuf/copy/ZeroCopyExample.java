package cn.xilio.netty.demo1_bytebuf.copy;

import java.nio.channels.*;
import java.io.*;

public class ZeroCopyExample {
    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();
        for (int i = 0; i <1; i++) {
            copy();
        }
        System.out.println("零拷贝耗时: " + (System.currentTimeMillis() - start) + "ms");
    }
    public static void copy() throws  Exception {

        try (FileChannel sourceChannel = new FileInputStream("dest.txt").getChannel();
             FileChannel destChannel = new FileOutputStream("source.txt").getChannel()) {
            sourceChannel.transferTo(0, sourceChannel.size(), destChannel); // 直接内核态传输
        }

    }
}
