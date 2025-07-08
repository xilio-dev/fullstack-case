package cn.xilio.jdk.thread.interupt;

/**
 * 不可中断synchronized阻塞
 */
public class InterruptSynchronizedDemo {
    public static void main(String[] args) throws InterruptedException {
        Object lock = new Object();

        // 持有锁的线程
        Thread holder = new Thread(() -> {
            synchronized (lock) {
                try {
                    System.out.println("锁持有者开始休眠...");
                    Thread.sleep(10_000); // 持有锁并休眠10s
                } catch (InterruptedException e) {
                    System.out.println("锁持有者被中断");
                }
            }
        });

        // 等待锁的线程. (测试是否可中断synchronized阻塞)
        Thread waiter = new Thread(() -> {
            System.out.println("等待者尝试获取锁...");
            synchronized (lock) {
                System.out.println("等待者获取到锁");
            }
        });

        holder.start();
        Thread.sleep(1000); //1s 确保 holder 先获取锁
        waiter.start();
        Thread.sleep(1000); //1s 等待 waiter 进入锁阻塞
        System.out.println("主线程调用 interrupt() 尝试中断等待者waiter");
        waiter.interrupt();
        System.out.println("等待者中断状态: " + waiter.isInterrupted());
    }
}
