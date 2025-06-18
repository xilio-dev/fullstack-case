package design_model.map.concurrenthashmap;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConcurrentHashMapSafePutDemo {
    public static void main(String[] args) throws InterruptedException {
        ConcurrentHashMap<Integer, Integer> map = new ConcurrentHashMap<>();
        ExecutorService executor = Executors.newFixedThreadPool(10);

        // 10个线程并发执行1000次put操作
        for (int i = 0; i < 10; i++) {
            final int threadId = i;
            executor.execute(() -> {
                for (int j = 0; j < 1000; j++) {
                    int key = threadId * 1000 + j;
                    map.put(key, key); // 单个put操作是线程安全的
                }
            });
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
            Thread.sleep(100);
        }

        System.out.println("预期size: " + (10 * 1000));
        System.out.println("实际size: " + map.size()); // 结果应为10000
    }
}
