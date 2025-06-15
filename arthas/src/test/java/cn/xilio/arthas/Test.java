package cn.xilio.arthas;

import com.taobao.middleware.cli.CLI;
import com.taobao.middleware.cli.CommandLine;
import com.taobao.middleware.cli.UsageMessageFormatter;
import com.taobao.middleware.cli.annotations.CLIConfigurator;
import com.taobao.middleware.cli.annotations.Name;
import com.taobao.middleware.cli.annotations.Summary;

import java.util.Arrays;
import java.util.logging.Level;

@Name("arthas-boot")
@Summary("Bootstrap Arthas")
public class Test {
    public static void main(String[] args) {
        Test test = new Test();
        CLI cli = CLIConfigurator.define(Test.class);
        CommandLine commandLine = cli.parse(Arrays.asList(args));
        try {
            CLIConfigurator.inject(commandLine, test);
        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(1);
        }
        AnsiLog.level(Level.ALL);
        AnsiLog.info("Process {} already using port {}", 1,2);
    }

}
