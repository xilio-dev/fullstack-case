package cn.xilio.jdk.design_model.map.concurrenthashmap;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConcurrentHashMapSafeSolution2 {
    public static void main(String[] args) throws InterruptedException {
        ConcurrentHashMap<Integer, Integer> map = new ConcurrentHashMap<>();
        ExecutorService executor = Executors.newFixedThreadPool(5);

        // 初始化900个元素
        for (int i = 0; i < 900; i++) {
            map.put(i, i);
        }

        // 5个线程并发执行补充100个元素的操作
        for (int i = 0; i < 5; i++) {
            executor.execute(() -> {
                synchronized (map) { // 添加同步块
                    HashMap<Integer, Integer> batch = new HashMap<>();
                    int currentSize = map.size();
                    int needToAdd = 1000 - currentSize;

                    if (needToAdd > 0) {
                        System.out.println(Thread.currentThread().getName() + " 补充: " + needToAdd);

                        for (int j = 0; j < needToAdd; j++) {
                            batch.put(900 + j, 900 + j);
                        }

                        map.putAll(batch);
                    }
                }
            });
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
            Thread.sleep(100);
        }

        System.out.println("预期size: 1000");
        System.out.println("实际size: " + map.size()); // 现在应为1000
    }
}
