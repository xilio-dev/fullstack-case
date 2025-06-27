package cn.xilio.arthas;

import java.util.ArrayList;
import java.util.List;

public class TableFormatter {
    public static String formatTable(List<String[]> rows) {
        // 1. 计算每列最大宽度
        int[] maxWidths = new int[rows.get(0).length];
        for (String[] row : rows) {
            for (int i = 0; i < row.length; i++) {
                maxWidths[i] = Math.max(maxWidths[i], row[i].trim().length());
            }
        }

        // 2. 构建对齐后的表格
        StringBuilder result = new StringBuilder();
        for (String[] row : rows) {
            for (int i = 0; i < row.length; i++) {
                String cell = row[i].trim();
                // 动态填充空格实现右对齐（数字列）或左对齐（文本列）
                String alignedCell = (i >= 3 && i <= 4) ?
                    String.format("%" + maxWidths[i] + "s", cell) :  // 端口号右对齐
                    String.format("%-" + maxWidths[i] + "s", cell);  // 其他列左对齐
                result.append(alignedCell).append("  "); // 双空格作为列间隔
            }
            result.append("\n");
        }
        return result.toString();
    }

    public static void main(String[] args) {
        // 原始数据（按行存储）
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"mysql", "TCP", "127.0.0.1", "3306", "3307", "4b0063baa5ae47c2910fc25265aae4b9"});
        rows.add(new String[]{"redis", "TCP", "127.0.0.1", "6379", "6380", "4b0063baa5ae47c2910fc25265aae4b9"});
        rows.add(new String[]{"web", "TCP", "127.0.0.1", "8080", "8085", "4b0063baa5ae47c2910fc25265aae4b9"});
        rows.add(new String[]{"postgre", "TCP", "127.0.0.1", "5432", "5433", "7c0084cbb6bf58d3021fd36376bbf5c0"});
        rows.add(new String[]{"http", "TCP", "127.0.0.1", "8080", "8081", "7c0084cbb6bf58d3021fd36376bbf5c0"});

        // 输出对齐结果
        System.out.println(formatTable(rows));
    }
}
