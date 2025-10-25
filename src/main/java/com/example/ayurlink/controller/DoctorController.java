package com.example.ayurlink.controller;

import com.example.ayurlink.model.Appointment;
import com.example.ayurlink.model.Doctor;
import com.example.ayurlink.model.DoctorEarning;
import com.example.ayurlink.model.Payment;
import com.example.ayurlink.service.AppointmentService;
import com.example.ayurlink.service.DoctorEarningService;
import com.example.ayurlink.service.DoctorService;
import com.example.ayurlink.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/doctor")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;
    private final AppointmentService appointmentService;
    private final PaymentService paymentService;
    private final DoctorEarningService doctorEarningService;

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        String username = auth.getName();
        Doctor doctor = doctorService.getDoctorByUsername(username)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        List<Appointment> appointments = appointmentService.getDoctorAppointments(doctor.getId());

        // Get earning summary
        Map<String, Object> earningSummary = doctorEarningService.getDoctorEarningSummary(doctor.getId());

        model.addAttribute("doctor", doctor);
        model.addAttribute("appointments", appointments);

        // Add earning data to model
        model.addAttribute("todayEarnings", earningSummary.get("todayEarnings"));
        model.addAttribute("weekEarnings", earningSummary.get("weekEarnings"));
        model.addAttribute("monthEarnings", earningSummary.get("monthEarnings"));
        return "doctor/dashboard";
    }

    @GetMapping("/appointments")
    public String viewAppointments(Authentication auth, Model model) {
        String username = auth.getName();
        Doctor doctor = doctorService.getDoctorByUsername(username)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        List<Appointment> appointments = appointmentService.getDoctorAppointments(doctor.getId());
        model.addAttribute("doctor", doctor);
        model.addAttribute("appointments", appointments);
        return "doctor/appointments";
    }
    @PostMapping("/appointments/{id}/complete")
    public String completeAppointment(@PathVariable Long id,
                                      Authentication auth,
                                      RedirectAttributes redirectAttributes) {
        try {
            String username = auth.getName();
            Doctor doctor = doctorService.getDoctorByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Doctor not found"));

            appointmentService.completeAppointment(id, doctor.getId());
            redirectAttributes.addFlashAttribute("success", "Appointment marked as completed");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/doctor/appointments";
    }
    @PostMapping("/appointments/{id}/cancel")
    public String cancelAppointment(@PathVariable Long id,
                                    Authentication auth,
                                    RedirectAttributes redirectAttributes) {
        try {
            String username = auth.getName();
            Doctor doctor = doctorService.getDoctorByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Doctor not found"));

            appointmentService.cancelAppointment(id, doctor.getId());
            redirectAttributes.addFlashAttribute("success", "Appointment cancelled successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/doctor/appointments";
    }

    @GetMapping("/profile")
    public String viewProfile(Authentication auth, Model model) {
        String username = auth.getName();
        Doctor doctor = doctorService.getDoctorByUsername(username)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        model.addAttribute("doctor", doctor);
        return "doctor/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute Doctor doctor,
                                Authentication auth,
                                RedirectAttributes redirectAttributes) {
        try {
            String username = auth.getName();
            Doctor existing = doctorService.getDoctorByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Doctor not found"));

            doctorService.updateDoctor(existing.getId(), doctor);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/doctor/profile";
    }

    @GetMapping("/availability")
    public String manageAvailability(Authentication auth, Model model) {
        String username = auth.getName();
        Doctor doctor = doctorService.getDoctorByUsername(username)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        model.addAttribute("doctor", doctor);
        return "doctor/availability";
    }

    @PostMapping("/availability/update")
    public String updateAvailability(@RequestParam List<String> availability,
                                     Authentication auth,
                                     RedirectAttributes redirectAttributes) {
        try {
            String username = auth.getName();
            Doctor doctor = doctorService.getDoctorByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Doctor not found"));

            doctorService.updateAvailability(doctor.getId(), availability);
            redirectAttributes.addFlashAttribute("success", "Availability updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/doctor/availability";
    }
    @GetMapping("/earnings")
    public String viewEarnings(Authentication auth, Model model) {
        String username = auth.getName();
        Doctor doctor = doctorService.getDoctorByUsername(username)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        // Get completed appointments for this doctor
        List<Appointment> completedAppointments = appointmentService
                .getCompletedAppointmentsByDoctor(doctor.getId());

        // Calculate total earnings from payments
        // Get comprehensive earning summary
        Map<String, Object> earningSummary = doctorEarningService.getDoctorEarningSummary(doctor.getId());

        // Get all earnings (both pending and settled)
        List<DoctorEarning> allEarnings = doctorEarningService.getDoctorEarnings(doctor.getId());

        // Calculate breakdown
        Double totalEarnings = (Double) earningSummary.get("totalEarnings");
        Double pendingAmount = (Double) earningSummary.get("pendingAmount");
        Double settledAmount = (Double) earningSummary.get("settledAmount");

        model.addAttribute("doctor", doctor);
        model.addAttribute("earningSummary", earningSummary);
        model.addAttribute("allEarnings", allEarnings);
        model.addAttribute("totalEarnings", totalEarnings);
        model.addAttribute("pendingAmount", pendingAmount);
        model.addAttribute("settledAmount", settledAmount);

        return "doctor/earnings";
    }
    // ADD NEW endpoint to view detailed earning breakdown
    @GetMapping("/earnings/details")
    public String viewEarningsDetails(Authentication auth,
                                      @RequestParam(required = false) String startDate,
                                      @RequestParam(required = false) String endDate,
                                      Model model) {
        String username = auth.getName();
        Doctor doctor = doctorService.getDoctorByUsername(username)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        List<DoctorEarning> earnings;

        if (startDate != null && endDate != null) {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            earnings = doctorEarningService.getDoctorEarningsByDateRange(doctor.getId(), start, end);
        } else {
            earnings = doctorEarningService.getDoctorEarnings(doctor.getId());
        }

        model.addAttribute("doctor", doctor);
        model.addAttribute("earnings", earnings);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "doctor/earnings-details";
    }
}