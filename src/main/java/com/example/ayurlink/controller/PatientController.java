package com.example.ayurlink.controller;

import com.example.ayurlink.model.*;
import com.example.ayurlink.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/patient")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;
    private final AppointmentService appointmentService;
    private final DoctorService doctorService;
    private final TreatmentService treatmentService;
    private final PaymentService paymentService;
    private final PasswordEncoder passwordEncoder;
    private final TimeSlotService timeSlotService;

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        String username = auth.getName();
        Patient patient = patientService.getPatientByUsername(username)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        List<Appointment> appointments = appointmentService.getPatientAppointments(patient.getId());

        // Calculate statistics
        long totalAppointments = appointments.size();

        // Upcoming: SCHEDULED + CONFIRMED + PENDING
        long upcomingAppointments = appointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED ||
                        a.getStatus() == AppointmentStatus.CONFIRMED ||
                        a.getStatus() == AppointmentStatus.PENDING)
                .count();

        // Completed
        long completedAppointments = appointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                .count();


        // Pending Payment: Only SCHEDULED (not yet paid)
        long pendingPayment = appointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED)
                .count();

        model.addAttribute("patient", patient);
        model.addAttribute("appointments", appointments);
        model.addAttribute("totalAppointments", totalAppointments);
        model.addAttribute("upcomingAppointments", upcomingAppointments);
        model.addAttribute("completedAppointments", completedAppointments);
        model.addAttribute("pendingPayment", pendingPayment);
        return "patient/dashboard";
    }



    @GetMapping("/book-appointment")
    public String showBookingForm(Model model) {
        List<Doctor> doctors = doctorService.getAllDoctors();
        List<Treatment> treatments = treatmentService.getActiveTreatments();

        model.addAttribute("doctors", doctors);
        model.addAttribute("treatments", treatments);
        return "patient/book-appointment";
    }
    @GetMapping("/available-slots")
    @ResponseBody
    public List<TimeSlotService.TimeSlotDTO> getAvailableSlots(
            @RequestParam Long doctorId,
            @RequestParam String date) {
        try {
            LocalDate appointmentDate = LocalDate.parse(date);

            // Validate doctor exists
            doctorService.getDoctorById(doctorId)
                    .orElseThrow(() -> new RuntimeException("Doctor not found"));

            return timeSlotService.getAvailableTimeSlotsWithStatus(doctorId, appointmentDate);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching available slots: " + e.getMessage());
        }
    }

    @PostMapping("/book-appointment")
    public String bookAppointment(@RequestParam Long doctorId,
                                  @RequestParam Long treatmentId,
                                  @RequestParam String appointmentDate,
                                  @RequestParam String appointmentTime,
                                  Authentication auth,
                                  RedirectAttributes redirectAttributes) {
        try {
            String username = auth.getName();
            Patient patient = patientService.getPatientByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Patient not found"));

            LocalDate date = LocalDate.parse(appointmentDate);
            LocalTime time = LocalTime.parse(appointmentTime);

            // Validate the time slot is still available (prevents double-booking)
            boolean isAvailable = timeSlotService.isTimeSlotAvailable(doctorId, date, time);
            if (!isAvailable) {
                redirectAttributes.addFlashAttribute("error",
                        "This time slot is no longer available. Please select another time.");
                return "redirect:/patient/book-appointment";
            }

            Appointment appointment = appointmentService.bookAppointment(
                    patient.getId(), doctorId, treatmentId, date, time
            );

            redirectAttributes.addFlashAttribute("success",
                    "Appointment booked successfully! Ticket Number: " + appointment.getTicketNumber());
            return "redirect:/patient/appointments";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/patient/book-appointment";
        }
    }

    @GetMapping("/appointments")
    public String viewAppointments(Authentication auth, Model model) {
        String username = auth.getName();
        Patient patient = patientService.getPatientByUsername(username)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        List<Appointment> appointments = appointmentService.getPatientAppointments(patient.getId());
        model.addAttribute("appointments", appointments);
        return "patient/appointments";
    }

    @PostMapping("/appointments/{id}/cancel")
    public String cancelAppointment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            appointmentService.cancelAppointment(id);
            redirectAttributes.addFlashAttribute("success", "Appointment cancelled successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/patient/appointments";
    }

    @GetMapping("/profile")
    public String viewProfile(Authentication auth, Model model) {
        String username = auth.getName();
        Patient patient = patientService.getPatientByUsername(username)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        model.addAttribute("patient", patient);
        return "patient/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute Patient patient,
                                Authentication auth,
                                RedirectAttributes redirectAttributes) {
        try {
            String username = auth.getName();
            Patient existing = patientService.getPatientByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Patient not found"));

            patientService.updatePatient(existing.getUsername(), patient);

            redirectAttributes.addFlashAttribute("success", "Profile updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/patient/profile";
    }

    @GetMapping("/payments")
    public String viewPayments(Authentication auth, Model model) {
        String username = auth.getName();
        Patient patient = patientService.getPatientByUsername(username)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        List<Payment> payments = paymentService.getPatientPayments(patient.getId());
        model.addAttribute("payments", payments);
        return "patient/payments";
    }
    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Principal principal,
            RedirectAttributes ra) {

        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("errorMessage", "New passwords do not match!");
            return "redirect:/patient/profile";
        }

        boolean changed = patientService.changePassword(principal.getName(), currentPassword, newPassword);
        if (changed) {
            ra.addFlashAttribute("successMessage", "Password updated successfully!");
        } else {
            ra.addFlashAttribute("errorMessage", "Current password is incorrect.");
        }
        return "redirect:/patient/profile";
    }


}