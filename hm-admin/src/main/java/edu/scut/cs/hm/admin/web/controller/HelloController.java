package edu.scut.cs.hm.admin.web.controller;

import edu.scut.cs.hm.admin.web.model.UiHeader;
import edu.scut.cs.hm.common.security.token.TokenException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HelloController {
    @RequestMapping("/")
    public String index() {
        return "index";
    }

    @RequestMapping("/hello")
    public String hello() {
        return "hello";
    }

    @RequestMapping("/login")
    public String login() {
        return "login";
    }
}
