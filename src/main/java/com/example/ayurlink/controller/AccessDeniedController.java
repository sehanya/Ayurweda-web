package com.example.ayurlink.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller to handle access denied scenarios and route users appropriately
 */
@Controller
@RequestMapping
public class AccessDeniedController {

    /**
     * Handle access denied scenarios (403 Forbidden)
     */
    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String userRole = "Guest";
        String dashboardUrl = "/";

        if (authentication != null && authentication.isAuthenticated()) {
            if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_PATIENT"))) {
                userRole = "Patient";
                dashboardUrl = "/patient/dashboard";
            } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_DOCTOR"))) {
                userRole = "Doctor";
                dashboardUrl = "/doctor/dashboard";
            } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
                userRole = "Admin";
                dashboardUrl = "/admin/dashboard";
            } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"))) {
                userRole = "Super Admin";
                dashboardUrl = "/superadmin/dashboard";
            }
        }

        model.addAttribute("userRole", userRole);
        model.addAttribute("dashboardUrl", dashboardUrl);

        return "access-denied";
    }

    /**
     * Handle general errors
     */
    @GetMapping("/error")
    public String error(Model model) {
        model.addAttribute("errorMessage", "An error occurred. Please try again.");
        return "error";
    }
}