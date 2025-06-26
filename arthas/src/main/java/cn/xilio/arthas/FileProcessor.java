package cn.xilio.arthas;

import org.apache.commons.cli.*;

public class FileProcessor {

    public static void main(String[] args) {
        // 1. 定义命令行选项
        Options options = new Options();

        // 添加帮助选项
        Option help = Option.builder("h")
                .longOpt("help")
                .desc("显示帮助信息")
                .build();

        // 添加版本选项
        Option version = Option.builder("v")
                .longOpt("version")
                .desc("显示版本信息")
                .build();

        // 添加输入文件选项（必需）
        Option input = Option.builder("i")
                .longOpt("input")
                .hasArg()
                .argName("文件路径")
                .required()
                .desc("输入文件路径")
                .build();

        // 添加输出文件选项（可选，有默认值）
        Option output = Option.builder("o")
                .longOpt("output")
                .hasArg()
                .argName("文件路径")
                .desc("输出文件路径（默认为output.txt）")
                .build();

        // 添加线程数选项
        Option threads = Option.builder("t")
                .longOpt("threads")
                .hasArg()
                .argName("数量")
                .type(Number.class)
                .desc("处理线程数（默认为4）")
                .build();

        // 添加详细日志选项
        Option verbose = Option.builder()
                .longOpt("verbose")
                .desc("启用详细日志")
                .build();

        // 将所有选项添加到Options对象
        options.addOption(help)
               .addOption(version)
               .addOption(input)
               .addOption(output)
               .addOption(threads)
               .addOption(verbose);

        // 2. 创建解析器和帮助格式化工具
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            // 3. 解析命令行参数
            cmd = parser.parse(options, args);

            // 4. 处理各种选项
            if (cmd.hasOption("help")) {
                formatter.printHelp("file-processor", options, true);
                return;
            }

            if (cmd.hasOption("version")) {
                System.out.println("文件处理工具 v1.0.0");
                return;
            }

            // 获取输入文件（必需）
            String inputFile = cmd.getOptionValue("input");
            System.out.println("输入文件: " + inputFile);

            // 获取输出文件（可选，带默认值）
            String outputFile = cmd.getOptionValue("output", "output.txt");
            System.out.println("输出文件: " + outputFile);

            // 获取线程数（可选，带默认值）
            int threadCount = Integer.parseInt(cmd.getOptionValue("threads", "4"));
            System.out.println("线程数: " + threadCount);

            // 检查详细日志选项
            if (cmd.hasOption("verbose")) {
                System.out.println("详细日志已启用");
            }

            // 5. 这里可以添加实际的业务逻辑
            processFiles(inputFile, outputFile, threadCount, cmd.hasOption("verbose"));

        } catch (ParseException e) {
            // 参数解析错误处理
            System.err.println("参数解析错误: " + e.getMessage());
            formatter.printHelp("file-processor", options);
            System.exit(1);
        } catch (NumberFormatException e) {
            // 线程数格式错误处理
            System.err.println("线程数必须是整数");
            formatter.printHelp("file-processor", options);
            System.exit(1);
        }
    }

    private static void processFiles(String input, String output, int threads, boolean verbose) {
        // 实际的文件处理逻辑
        System.out.println("开始处理文件...");
        if (verbose) {
            System.out.println("详细日志: 正在读取文件 " + input);
        }
        // 模拟处理过程
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("文件处理完成，结果已保存到 " + output);
    }
}
