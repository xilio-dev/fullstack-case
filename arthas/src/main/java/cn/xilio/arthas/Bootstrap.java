package cn.xilio.arthas;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Bootstrap {
    public static void main(String[] args) throws Exception {
        Class<TelnetConsole> clazz = TelnetConsole.class;
        Method mainMethod = clazz.getMethod("main", String[].class);
        List<String> telnetArgs = new ArrayList<String>();

        mainMethod.invoke(null, new Object[] { telnetArgs.toArray(new String[0]) });
    }
}
