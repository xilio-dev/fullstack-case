package cn.xilio.jdk.thread.forkjoin;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class ForkJoinException extends RecursiveAction {
    @Override
    protected void compute() {
        try {
            System.out.println("进入计算方法了");
            int a = 1 / 0;
            System.out.println("hello world");
        } catch (Exception e) {
            // 方式1：直接在这里处理异常
            //System.err.println("任务内部捕获异常: " + e);
            // 重新抛出以便外部也能检测到
            throw e;
        }
    }

    /**
     * 需要立即获取结果时用invoke()
     * 需要异步执行时用submit()
     * 需要批量提交时用execute()
     */
    public static void main(String[] args) throws InterruptedException {
        ForkJoinException task = new ForkJoinException();
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        forkJoinPool.submit(task);
        // 等待任务完成,不然无法看到后面的异常信息
        while (!task.isDone()) {
            Thread.sleep(1000);
        }
        if (task.isCompletedAbnormally()) {
            System.out.println("1、任务执行异常");
            // 获取并打印具体异常
            Throwable exception = task.getException();
            if (exception != null) {
                System.err.println("2、异常详情: " + exception);
            }
        }
    }

}
