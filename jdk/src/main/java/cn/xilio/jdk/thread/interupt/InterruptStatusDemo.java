package cn.xilio.jdk.thread.interupt;

/**
 * 判断线程是否为停止状态
 */
public class InterruptStatusDemo {
   private static class MyThread extends Thread {
       @Override
       public void run() {
           super.run();
           for (int i = 1; i < 50_0000; i++) {
              // System.out.println("i=" + i);
           }
       }
   }
    public static void main(String[] args) {
        MyThread thread = new MyThread();
        try {
            thread.start();
            Thread.sleep(1000);
            thread.interrupt(); // 中断线程
            System.out.println("是否中断线程1:"+thread.isInterrupted());//false
            System.out.println("是否中断线程2:"+thread.isInterrupted());//false
        } catch (InterruptedException e) {
            System.out.println("main catch");
        }

    }
}
