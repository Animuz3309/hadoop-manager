package edu.scut.cs.hm.admin.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MainController {

    @RequestMapping("/")
    public String index() {
        return "redirect:dashboard";
    }

    @RequestMapping("/login")
    public String login() {
        return "login";
    }
}
