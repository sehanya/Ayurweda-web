package com.example.AyurvedaWeb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/treatment")

public class TreatmentController {

    @GetMapping("/Treatment")
    public String ayurvedaInfo()
    {
        return "Treatment";

    }
}
