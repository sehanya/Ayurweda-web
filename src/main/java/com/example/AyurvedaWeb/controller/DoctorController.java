package com.example.AyurvedaWeb.controller;

import com.example.AyurvedaWeb.model.Doctor;
import com.example.AyurvedaWeb.service.DoctorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/doctors")
public class DoctorController {

    private final DoctorService service;

    public DoctorController(DoctorService service) {
        this.service = service;
    }

    // Show all doctors
    @GetMapping
    public String listDoctors(Model model) {
        List<Doctor> doctors = service.getAllDoctors();
        model.addAttribute("doctors", doctors);
        model.addAttribute("doctor", new Doctor()); // empty form object
        return "/admin/dashboard"; // your Thymeleaf page (e.g., dashboard.html)
    }

    // Add new doctor
    @PostMapping("/add")
    public String addDoctor(@ModelAttribute Doctor doctor) {
        service.addDoctor(doctor);
        return "redirect:/admin/dashboard"; // refresh list
    }

    // Update doctor
    @PostMapping("/update/{id}")
    public String updateDoctor(@PathVariable Long id, @ModelAttribute Doctor doctor) {
        service.updateDoctor(id, doctor);
        return "redirect:/admin/dashboard";
    }

    // Deactivate doctor (set active=false)
    @PostMapping("/deactivate/{id}")
    public String deactivateDoctor(@PathVariable Long id) {
        service.getDoctorById(id).ifPresent(d -> {
            d.setActive(false);
            service.addDoctor(d);
        });
        return "redirect:/admin/dashboard";
    }

    // Delete doctor
    @PostMapping("/delete/{id}")
    public String deleteDoctor(@PathVariable Long id) {
        service.deleteDoctor(id);
        return "redirect:/admin/dashboard";
    }
}
