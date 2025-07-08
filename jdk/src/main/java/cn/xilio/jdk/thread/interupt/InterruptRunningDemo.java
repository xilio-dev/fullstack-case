package cn.xilio.jdk.thread.interupt;

/**
 * 协作式中断（通过检查中断状态）
 */
public class InterruptRunningDemo {
    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(() -> {
            int count = 0;
            while (!Thread.currentThread().isInterrupted()) {
                System.out.println("线程运行中，计数: " + count++);
                try {
                    Thread.sleep(1000); // 模拟工作
                } catch (InterruptedException e) {
                    System.out.println("捕获到 InterruptedException，退出循环");
                    break; // 退出循环
                }
            }
            System.out.println("线程退出，当前中断状态: " + Thread.currentThread().isInterrupted());
        });

        thread.start();
        Thread.sleep(3000); // 主线程等待 3 秒
        System.out.println("主线程调用 interrupt()");
        thread.interrupt();
    }
}
