package cn.xilio.jdk.design_model.map.concurrenthashmap;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConcurrentHashMapSafeSolution1 {
    public static void main(String[] args) throws InterruptedException {
        ConcurrentHashMap<Integer, Integer> map = new ConcurrentHashMap<>();
        ExecutorService executor = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 10; i++) {
            executor.execute(() -> {
                for (int j = 0; j < 1000; j++) {
                    int randomNum = (int) (Math.random() * 10);

                    // 使用compute方法保证原子性
                    map.compute(randomNum, (k, v) -> v == null ? 1 : v + 1);
                }
            });
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
            Thread.sleep(100);
        }

        int total = map.values().stream().mapToInt(Integer::intValue).sum();
        System.out.println("预期总次数: " + (10 * 1000));
        System.out.println("实际总次数: " + total); // 现在应为10000
        System.out.println("各数字出现次数: " + map);
    }
}
