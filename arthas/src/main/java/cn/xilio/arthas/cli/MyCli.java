package cn.xilio.arthas.cli;

import com.taobao.middleware.cli.CLI;
import com.taobao.middleware.cli.CLIs;
import com.taobao.middleware.cli.CommandLine;
import com.taobao.middleware.cli.annotations.*;

import java.util.Arrays;
@Name("proxy:\n")
@Summary("代理配置:\ndsdsd")
@Description("案例:\n" + " help\n" + " help sc\n" + " help sm\n" + " help watch")

public class MyCli {

    private boolean verbose;
    private String filename;
    private String doc;

    @Description("文件名")
    @Option(shortName = "v", flag = true, longName = "verbose", help = true)
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
    @Description("文件路径")
    @Argument(index = 0, required = true)
    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Option(shortName = "d", longName = "doc", help = true)
    public void setDoc(String doc) {
        this.doc = doc;
    }

    public static void main(String[] args) {
        // 使用
        CLI cli = CLIs.create(MyCli.class);
        CommandLine cmd = cli.parse(Arrays.asList("-v", "test.txt"));
        MyCli instance = new MyCli();
        CLIConfigurator.inject(cmd, instance);


        StringBuilder builder = new StringBuilder();
        cli.usage(builder);
        System.out.println(builder.toString());
    }
}
