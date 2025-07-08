package cn.xilio.jdk.thread.interupt;

/**
 * yield作用是放弃 当前cpu资源 让其他任务去占用cpu执行时间片，时间不确定。
 * 下面测试案例使用yield后会使线程的执行耗时指数级增长，从10毫秒左右增长到4秒左右
 */
public class YieldDemo {

    public static void main(String[] args) {
        new Thread(() -> {
            long startTime = System.currentTimeMillis();
            int count = 0;
            for (int i = 0; i < 1000_0000; i++) {
                //Thread.yield(); //将其注释以后，耗时几毫秒（10ms以下），不注释耗时几秒（接近4s），差别很大
                count = count + (i + 1);
            }
            long endTime = System.currentTimeMillis();
            System.out.println("结果: " + count);
            System.out.println("耗时: " + (endTime - startTime) + " ms");
        }).start();
    }
}
