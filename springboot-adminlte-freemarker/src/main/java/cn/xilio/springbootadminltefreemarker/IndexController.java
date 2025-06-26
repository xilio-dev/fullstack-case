package cn.xilio.springbootadminltefreemarker;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
@Controller
@RequestMapping("/")
public class IndexController {
    @GetMapping("/index")
    public String index(){
        return "index";
    }
    @GetMapping("/index3")
    public String index3(){
        return "index3";
    }
    @GetMapping("/tables/simple")
    public String tables_simple(){
        return "tables/simple";
    }
}
