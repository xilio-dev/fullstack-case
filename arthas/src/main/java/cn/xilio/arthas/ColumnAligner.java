package cn.xilio.arthas;

import java.util.ArrayList;
import java.util.List;

public class ColumnAligner {
    public static void main(String[] args) {
        String input = "redisdee  TCP 127.0.0.1 2345      3307       4b0063baa5ae47c2910fc25265aae4b9\n" +
                      "mysql  TCP 127.0.0.13306      3307       4b0063baa5ae47c2910fc25265aae4b9";
        alignColumns(input);
    }

    public static void alignColumns(String input) {
        String[] rows = input.split("\n");
        List<String[]> table = new ArrayList<>();
        int maxCols = 0;

        // 1. 分割每行数据并记录最大列数
        for (String row : rows) {
            String[] cols = row.split("\\s+");
            table.add(cols);
            maxCols = Math.max(maxCols, cols.length);
        }

        // 2. 计算每列的最大宽度
        int[] colWidths = new int[maxCols];
        for (String[] cols : table) {
            for (int i = 0; i < cols.length; i++) {
                colWidths[i] = Math.max(colWidths[i], getDisplayWidth(cols[i]));
            }
        }

        // 3. 格式化输出对齐后的表格
        for (String[] cols : table) {
            StringBuilder alignedRow = new StringBuilder();
            for (int i = 0; i < cols.length; i++) {
                String format = "%-" + (colWidths[i] + 2) + "s"; // 左对齐，间距2
                alignedRow.append(String.format(format, cols[i]));
            }
            System.out.println(alignedRow.toString().trim());
        }
    }

    // 4. 处理中英文混合字符的显示宽度（中文占2字符）
    private static int getDisplayWidth(String s) {
        int width = 0;
        for (char c : s.toCharArray()) {
            width += (c >= '\u4e00' && c <= '\u9fa5') ? 2 : 1; // 简单判断中文字符
        }
        return width;
    }
}
