package com.example.AyurvedaWeb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @GetMapping("/admin")
    public String admin()
    {
        return "dashboard";

    }
    @GetMapping("/superadmin")
    public String superadmin()
    {
        return "dashboard";

    }
}