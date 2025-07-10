package cn.xilio.jdk.natives;
public class HelloJNI {
    // 声明Native方法（无实现体）
    public native void sayHello();
    // 静态代码块：加载动态链接库
    static {
        // 库名需与后续生成的库文件名一致（不含扩展名）
        System.loadLibrary("helloJNI");
    }

    public static void main(String[] args) {
        // 调用Native方法
        new HelloJNI().sayHello();
    }
}
