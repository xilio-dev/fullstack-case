package cn.xilio.jdk.thread.interupt;

/**
 * 可中断Thread.join阻塞
 */
public class InterruptJoinDemo {
    public static void main(String[] args) throws InterruptedException {
        Thread worker = new Thread(() -> {
            try {
                System.out.println("Worker 线程开始执行，模拟工作...");
                Thread.sleep(20_000); // 模拟长时间运行 20s
                System.out.println("Worker 线程完成");
            } catch (InterruptedException e) {
                System.out.println("Worker 线程被中断");
            }
        });

        Thread waiter = new Thread(() -> {
            try {
                System.out.println("Waiter 线程等待 Worker 线程...");
                worker.join(); // 等待 worker 线程结束(在结束前调用中断进行测试)
                System.out.println("Waiter 线程继续执行");
            } catch (InterruptedException e) {
                System.out.println("Waiter 线程被中断，捕获到 InterruptedException");
                System.out.println("Waiter 中断状态: " + Thread.currentThread().isInterrupted());
            }
        });

        worker.start();
        waiter.start();
        Thread.sleep(2000); // 主线程等待 2 秒
        System.out.println("主线程调用 waiter.interrupt()");
        waiter.interrupt(); // 中断 waiter 线程
    }
}
