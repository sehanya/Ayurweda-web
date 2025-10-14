package com.example.ayurlink.controller;

import com.example.ayurlink.model.Patient;
import com.example.ayurlink.model.User;
import com.example.ayurlink.repository.UserRepository;
import com.example.ayurlink.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final PatientService patientService;
    private final UserRepository userRepository;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("patient", new Patient());
        return "register";
    }

    @PostMapping("/register")
    public String registerPatient(@Valid @ModelAttribute("patient") Patient patient,
                                  BindingResult result,
                                  RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "register";
        }

        try {
            patientService.registerPatient(patient);
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        // Get user role and redirect to appropriate dashboard
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("");

        return switch (role) {
            case "ROLE_PATIENT" -> "redirect:/patient/dashboard";
            case "ROLE_DOCTOR" -> "redirect:/doctor/dashboard";
            case "ROLE_ADMIN" -> "redirect:/admin/dashboard";
            case "ROLE_SUPER_ADMIN" -> "redirect:/superadmin/dashboard";
            default -> "redirect:/login";
        };
    }
}