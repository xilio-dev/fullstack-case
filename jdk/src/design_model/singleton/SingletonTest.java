package design_model.singleton;

public class SingletonTest {
    public static void main(String[] args) {
        // 创建多个线程尝试获取单例实例
        for (int i = 0; i < 50; i++) {
            new Thread(() -> {
                UnsafeSingleton singleton = UnsafeSingleton.getInstance();
                System.out.println("Singleton hashcode: " + System.identityHashCode(singleton));
            }).start();
        }
    }
}
