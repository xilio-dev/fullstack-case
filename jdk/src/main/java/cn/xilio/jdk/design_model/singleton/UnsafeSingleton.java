package cn.xilio.jdk.design_model.singleton;

/**
 * 单列模式
 */
public class UnsafeSingleton {
    private       static UnsafeSingleton instance;

    private UnsafeSingleton() {
        // 私有构造函数防止外部实例化
    }

    public static UnsafeSingleton getInstance() {
        if (instance == null) { // 第一次检查
            synchronized (UnsafeSingleton.class) {
                //如果不进行第二次检查，那么在多线程的情况下，会有多个实例被创建出来
                instance = new UnsafeSingleton(); // 直接创建实例
            }
        }
        return instance;
    }
}
