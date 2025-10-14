package com.example.ayurlink.controller;

import com.example.ayurlink.model.Treatment;
import com.example.ayurlink.service.TreatmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final TreatmentService treatmentService;

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        List<Treatment> treatments = treatmentService.getActiveTreatments();
        model.addAttribute("treatments", treatments);
        return "home";
    }

    @GetMapping("/treatments")
    public String treatments(Model model) {
        List<Treatment> treatments = treatmentService.getActiveTreatments();
        model.addAttribute("treatments", treatments);
        return "treatments";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }
}