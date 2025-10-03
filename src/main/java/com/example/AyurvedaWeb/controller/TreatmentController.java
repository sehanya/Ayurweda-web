package com.example.AyurvedaWeb.controller;

import com.example.AyurvedaWeb.DTO.TreatmentDTO;
import com.example.AyurvedaWeb.model.Treatment;
import com.example.AyurvedaWeb.service.TreatmentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/treatments")
public class TreatmentController {

    private final TreatmentService service;

    public TreatmentController(TreatmentService service) {
        this.service = service;
    }

    // Show all treatments page (for Thymeleaf or HTML dashboard)
    @GetMapping
    public String listTreatments(Model model) {
        List<Treatment> treatments = service.getAllTreatments();
        model.addAttribute("treatments", treatments);
        model.addAttribute("treatmentDTO", new TreatmentDTO());
        return "/admin/dashboard"; // your HTML page name (treatments.html)
    }

    // Add new treatment (works with HTML form)
    @PostMapping("/add")
    public String addTreatment(@ModelAttribute TreatmentDTO dto) {
        service.createTreatment(dto);
        return "redirect:/admin/dashboard"; // redirect to same page to see updated list
    }

    // Update treatment
    @PostMapping("/update/{id}")
    public String updateTreatment(@PathVariable Long id, @ModelAttribute TreatmentDTO dto) {
        service.updateTreatment(id, dto);
        return "redirect:/admin/dashboard";
    }

    // Deactivate treatment
    @PostMapping("/deactivate/{id}")
    public String deactivateTreatment(@PathVariable Long id) {
        service.deactivateTreatment(id);
        return "redirect:/admin/dashboard";
    }

    // Delete treatment
    @PostMapping("/delete/{id}")
    public String deleteTreatment(@PathVariable Long id) {
        service.deleteTreatment(id);
        return "redirect:/admin/dashboard";
    }
}
