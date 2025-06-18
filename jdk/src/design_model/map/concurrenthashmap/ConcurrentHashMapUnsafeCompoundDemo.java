package design_model.map.concurrenthashmap;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConcurrentHashMapUnsafeCompoundDemo {
    public static void main(String[] args) throws InterruptedException {
        ConcurrentHashMap<Integer, Integer> map = new ConcurrentHashMap<>();
        ExecutorService executor = Executors.newFixedThreadPool(10);

        // 模拟统计随机数出现次数的场景
        for (int i = 0; i < 10; i++) {
            executor.execute(() -> {
                for (int j = 0; j < 1000; j++) {
                    int randomNum = (int) (Math.random() * 10); // 生成0-9的随机数

                    // 复合操作: 检查-计算-更新
                    if (map.containsKey(randomNum)) {
                        map.put(randomNum, map.get(randomNum) + 1);
                    } else {
                        map.put(randomNum, 1);
                    }
                }
            });
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
            Thread.sleep(100);
        }

        // 计算总次数
        int total = map.values().stream().mapToInt(Integer::intValue).sum();
        System.out.println("预期总次数: " + (10 * 1000));
        System.out.println("实际总次数: " + total); // 结果可能小于10000
        System.out.println("各数字出现次数: " + map);
    }
}
