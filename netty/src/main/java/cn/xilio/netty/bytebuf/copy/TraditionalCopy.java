package cn.xilio.netty.bytebuf.copy;

import java.io.*;

public class TraditionalCopy {
    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1; i++) {
            copu();
        }
        System.out.println("传统IO耗时: " + (System.currentTimeMillis() - start) + "ms");
    }
    public static void copu() throws  Exception {

        try (FileInputStream fis = new FileInputStream("dest.txt");
             FileOutputStream fos = new FileOutputStream("source.txt")) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead); // 用户态与内核态间拷贝
            }
        }

    }
}
