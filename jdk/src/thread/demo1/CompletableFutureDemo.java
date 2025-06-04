package thread.demo1;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CompletableFutureDemo {
    public static void main(String[] args) throws Exception {

//        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println(Thread.currentThread().getName());
//            return "hello";
//        });
//        System.out.println(future.get());
//        String aDefault = future.getNow("default");
//        System.out.println(aDefault);
//        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> "hello");
//        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> "world");
//        future1.thenCombine(future2, (s1, s2) -> s1 + " " + s2)
//                .thenAccept(System.out::println);

//        CompletableFuture.supplyAsync(() -> {
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }
//                    return "hello";
//                }).thenApply(s -> s + " world")//同步组合上一步的结果
//                .thenApplyAsync(s -> s + "!")//异步组合上一步的结果
//                .thenAccept(System.out::println);//hello world!
//        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
//            try {Thread.sleep(1000);} catch (InterruptedException ignored) {}
//            return "result1";
//        });
//        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
//            try {Thread.sleep(1500);} catch (InterruptedException ignored) {}
//            return "result2";
//        });CompletableFuture<String> future3 = CompletableFuture.supplyAsync(() -> {
//            try {Thread.sleep(500);} catch (InterruptedException ignored) {}
//            return "result3";
//        });
//        CompletableFuture<Void> futures = CompletableFuture.allOf(future1, future2, future3);
//        futures.thenRun(() -> {
//            System.out.println("所有任务已经执行完成～");
//            try {
//                System.out.println(future1.get());
//                System.out.println(future2.get());
//                System.out.println(future3.get());
//            }  catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
        CompletableFuture.supplyAsync(() -> {
            if (new Random().nextBoolean()) {
                throw new RuntimeException("出错了");
            }
            return "结果";
        }).exceptionally(e -> {
            System.out.println("error" + e.getMessage());
            return "默认结果";
        }).thenAccept(System.out::println);


        Thread.sleep(3000);
    }
}
