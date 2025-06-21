package array;

import java.util.Arrays;

// 性能测试示例
public class ArrayCopyPerformance {
    public static void main(String[] args) {
        int[] source = new int[1000000];
        int[] dest = new int[1000000];

        // System.arraycopy
        long start = System.nanoTime();
        System.arraycopy(source, 0, dest, 0, source.length);
        long time1 = System.nanoTime() - start;

        // for循环
        start = System.nanoTime();
        for (int i = 0; i < source.length; i++) {
            dest[i] = source[i];
        }
        long time2 = System.nanoTime() - start;

        System.out.println("System.arraycopy: " + time1 + " ns");
        System.out.println("for loop: " + time2 + " ns");
    }
}
