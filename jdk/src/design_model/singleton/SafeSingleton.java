package design_model.singleton;

public class SafeSingleton {
    // 使用volatile确保可见性和禁止指令重排序
    private static volatile SafeSingleton instance;

    private SafeSingleton() {
        // 私有构造函数防止外部实例化
    }

    public static SafeSingleton getInstance() {
        // 第一次检查：避免不必要的同步
        if (instance == null) {
            synchronized (SafeSingleton.class) {
                // 第二次检查：确保只有一个实例被创建
                if (instance == null) {
                    instance = new SafeSingleton();
                }
            }
        }
        return instance;
    }
}
