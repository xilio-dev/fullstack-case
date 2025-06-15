package cn.xilio.arthas;

import com.taobao.text.Color;
import com.taobao.text.Decoration;
import com.taobao.text.ui.TableElement;
import com.taobao.text.util.RenderUtil;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;

import static com.taobao.text.ui.Element.label;

public class LogoTest {
    public static void main(String[] args) throws IOException {

        FileInputStream fileReader = new FileInputStream("/Users/liuxin/Desktop/dev-scene/arthas/src/main/resources/logo.txt");
        String logoText = IOUtils.toString(fileReader);
        StringBuilder sb = new StringBuilder();
        String[] LOGOS = new String[6];
        int i = 0, j = 0;
        for (String line : logoText.split("\n")) {
            sb.append(line);
            sb.append("\n");
            if (i++ == 6) {
                LOGOS[j++] = sb.toString();
                i = 0;
                sb.setLength(0);
            }
        }

        TableElement logoTable = new TableElement();
        logoTable.row(label(LOGOS[0]).style(Decoration.bold.fg(Color.red)),
                label(LOGOS[1]).style(Decoration.bold.fg(Color.yellow)),
                label(LOGOS[2]).style(Decoration.bold.fg(Color.cyan)),
                label(LOGOS[3]).style(Decoration.bold.fg(Color.magenta)));
        String render = RenderUtil.render(logoTable);
        System.out.println(render);
    }
}
