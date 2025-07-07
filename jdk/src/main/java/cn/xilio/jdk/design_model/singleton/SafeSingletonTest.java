package cn.xilio.jdk.design_model.singleton;

public class SafeSingletonTest {
    public static void main(String[] args) {
        for (int i = 0; i < 50; i++) {
            new Thread(() -> {
                SafeSingleton singleton = SafeSingleton.getInstance();
                System.out.println("Singleton hashcode: " + System.identityHashCode(singleton));
            }).start();
        }
    }
}
