package cn.xilio.jdk.thread.interupt;

import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 不可终端IO阻塞
 */
public class InterruptIODemo {
    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(8080);
        Thread thread = new Thread(() -> {
            try {
                System.out.println("线程等待客户端连接...");
                Socket socket = serverSocket.accept(); // I/O 阻塞
                System.out.println("客户端已连接");
                socket.close();
            } catch (Exception e) {
                System.out.println("捕获到异常: " + e.getMessage());
            }
        });

        thread.start();
        Thread.sleep(2000); // 主线程等待 2 秒
        System.out.println("主线程调用 interrupt()");
        thread.interrupt(); // 尝试中断
        System.out.println("线程中断状态: " + thread.isInterrupted());
        serverSocket.close(); // 手动关闭以结束阻塞
    }
}
