package edu.scut.cs.hm.admin.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DashboardController {

    @ModelAttribute("clusters")
    public Integer clusters() {
        return 3;
    }

    @ModelAttribute("nodes")
    public Integer nodes() {
        return 20;
    }

    @ModelAttribute("containers")
    public Integer containers() {
        return 120;
    }

    @ModelAttribute("services")
    public Integer services() {
        return 16;
    }

    @RequestMapping("/dashboard")
    public String index() {
        return "dashboard/index";
    }
}
