package cn.xilio.jdk.thread.interupt;

import java.util.concurrent.locks.LockSupport;

/**
 * 可中断LockSupport.park阻塞，但是后续的代码还会继续执行，无InterruptedException
 * 同时，中断状态不会被清除，和其他中断有所不同
 *
 */
public class InterruptParkDemo {
    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(() -> {
            System.out.println("线程进入 park 状态...");
            LockSupport.park(); // 线程挂起，如果不调用中断会一致挂起在这里；调用以后会返回同时后面的代码也会执行
            System.out.println("线程从 park 返回");
            System.out.println("线程中断状态: " + Thread.currentThread().isInterrupted());//中断状态不会被清除
        });

        thread.start();
        Thread.sleep(2000); // 主线程等待 2 秒
        System.out.println("主线程调用 interrupt()");
        thread.interrupt(); // 中断线程
    }
}
