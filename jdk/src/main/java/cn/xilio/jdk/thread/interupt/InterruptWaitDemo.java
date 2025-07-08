package cn.xilio.jdk.thread.interupt;

/**
 * 可中断wait阻塞
 * InterruptedException
 */
public class InterruptWaitDemo {
    public static void main(String[] args) throws InterruptedException {
        Object lock = new Object();
        Thread thread = new Thread(() -> {
            synchronized (lock) {
                try {
                    System.out.println("线程进入等待状态...");
                    lock.wait(); // 无限期等待，中断后立即返回
                    System.out.println("线程被唤醒");
                } catch (InterruptedException e) {
                    System.out.println("线程被中断，捕获到 InterruptedException");
                    System.out.println("当前中断状态: " + Thread.currentThread().isInterrupted());
                }
            }
        });

        thread.start();
        Thread.sleep(2000); // 主线程等待 2 秒
        System.out.println("主线程调用 interrupt()");
        thread.interrupt(); // 中断线程
    }
}
