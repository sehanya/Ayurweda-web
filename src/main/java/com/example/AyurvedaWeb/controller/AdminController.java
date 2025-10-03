package com.example.AyurvedaWeb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {


    @GetMapping("/dashboard")
    public String admin()
    {
        return "admin/dashboard";

    }
    @GetMapping("/superadmin")
    public String superadmin()
    {
        return "superadmin/dashboard";

    }
}