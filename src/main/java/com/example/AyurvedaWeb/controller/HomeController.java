package com.example.AyurvedaWeb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequestMapping("/home")
public class HomeController {
    @GetMapping("/")
    public String home()
    {
        return "home";
    }
    @GetMapping("/history")
    public String history() {
        return "history";
    }

}








