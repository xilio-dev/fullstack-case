package cn.xilio.jdk.thread.interupt;

/**
 * 中断main线程
 *
 * interrupted具有清除状态的功能，所以两次调用结果会不一样，而isInterrupted没有清除状态的功能
 * interrupted是Thread的静态方法，isInterrupted属于线程的this
 */
public class InterruptMainDemo {
    public static void main(String[] args) {
        Thread.currentThread().interrupt();
        boolean interrupted = Thread.interrupted();
        boolean interrupted2 = Thread.interrupted();
        System.out.println("是否停止1:"+interrupted);//true
        System.out.println("是否停止2:"+interrupted2);//false 前面调用后会清除状态
        System.out.println("end");
    }
}
