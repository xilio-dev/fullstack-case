package cn.xilio.jdk.thread.interupt;

/**
 * 可中断Thread.sleep阻塞
 * InterruptedException
 * sleep和interrupt()的调用顺序没有影响
 * Thread.currentThread().interrupt() 用于恢复中断状态，一般在发生InterruptedException后调用
 */
public class InterruptSleepDemo {
    public static void main(String[] args) throws InterruptedException {
        /**
         * 下面案例是先调用sleep再调用interrupt()，不管顺序如何追踪的结果都是一样的，interrupt()只是设置一个标记。
         */
        Thread thread = new Thread(() -> {
            try {
                System.out.println("线程开始休眠 10 秒...");
                Thread.sleep(10_000); // 休眠 10 秒
                System.out.println("线程休眠结束");
            } catch (InterruptedException e) {
                System.out.println("线程被中断，捕获到 InterruptedException");
                System.out.println("当前中断状态: " + Thread.currentThread().isInterrupted());
            }
        });
        Thread thread2 = new Thread(() -> {
            try {
                //先循环一下，让interrupt先被调用，以测试顺序是否有影响（结论：无影响）
                for (int i = 0; i < 100_0000; i++) {
                    System.out.println(i);
                }
                System.out.println("线程开始休眠 10 秒...");
                Thread.sleep(10_000); // 休眠 10 秒
                System.out.println("线程休眠结束");
            } catch (InterruptedException e) {
                System.out.println("线程被中断，捕获到 InterruptedException");
                System.out.println("当前中断状态: " + Thread.currentThread().isInterrupted());
            }
        });

        thread.start();
        Thread.sleep(2000); // 主线程等待 2 秒
        System.out.println("主线程调用 interrupt()");
        thread.interrupt(); // 中断线程
    }
}
