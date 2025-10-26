package com.example.ayurlink.controller;

import com.example.ayurlink.model.*;
import com.example.ayurlink.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.core.io.Resource;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final TreatmentService treatmentService;
    private final DoctorService doctorService;
    private final AdminService adminService;
    private final AppointmentService appointmentService;
    private final PaymentService paymentService;
    private final FileStorageService fileStorageService;
    private final DoctorEarningService doctorEarningService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<Treatment> activeTreatments = treatmentService.getActiveTreatments();
        List<Doctor> doctors = doctorService.getAllDoctors();

        // Get all appointments
        List<Appointment> allAppointments = appointmentService.getAllAppointments();

        // Count today's appointments
        LocalDate today = LocalDate.now();
        long todayAppointmentCount = allAppointments.stream()
                .filter(apt -> apt.getAppointmentDate().equals(today))
                .count();

        // Count total unique patients from appointments
        long totalPatientCount = allAppointments.stream()
                .map(Appointment::getPatient)
                .filter(patient -> patient != null)
                .map(Patient::getId)
                .distinct()
                .count();

        model.addAttribute("treatmentCount", activeTreatments.size());
        model.addAttribute("doctorCount", doctors.size());
        model.addAttribute("todayAppointmentCount", todayAppointmentCount);
        model.addAttribute("totalPatientCount", totalPatientCount);

        return "admin/dashboard";
    }

    // ==================== TREATMENT MANAGEMENT ====================

    @GetMapping("/treatments")
    public String listTreatments(Model model) {
        List<Treatment> treatments = treatmentService.getActiveTreatments();
        model.addAttribute("treatments", treatments);
        return "admin/treatments";
    }

    @GetMapping("/treatments/new")
    public String showTreatmentForm(Model model) {
        model.addAttribute("treatment", new Treatment());
        return "admin/treatment-form";
    }

    @PostMapping("/treatments/save")
    public String saveTreatment(@ModelAttribute Treatment treatment,@RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                RedirectAttributes redirectAttributes) {
        try {
            if (treatment.getId() == null) {
                treatmentService.createTreatment(treatment,imageFile);
                redirectAttributes.addFlashAttribute("success", "Treatment created successfully");
            } else {
                treatmentService.updateTreatment(treatment.getId(), treatment,imageFile);
                redirectAttributes.addFlashAttribute("success", "Treatment updated successfully");
            }
            return "redirect:/admin/treatments";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/treatments/new";
        }
    }

    @GetMapping("/treatments/edit/{id}")
    public String editTreatment(@PathVariable Long id, Model model) {
        Treatment treatment = treatmentService.getTreatmentById(id)
                .orElseThrow(() -> new RuntimeException("Treatment not found"));
        model.addAttribute("treatment", treatment);
        return "admin/treatment-form";
    }

    @PostMapping("/treatments/delete/{id}")
    public String deleteTreatment(@PathVariable Long id,
                                  RedirectAttributes redirectAttributes) {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║  DELETE REQUEST RECEIVED!              ║");
        System.out.println("║  Treatment ID: " + id + "                        ║");
        System.out.println("╚════════════════════════════════════════╝");
        try {
            treatmentService.deleteTreatment(id);
            System.out.println("✓ Delete successful!");
            redirectAttributes.addFlashAttribute("success", "Treatment deleted successfully");
        } catch (Exception e) {
            System.err.println("✗ Delete failed: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error","Failed to delete: " + e.getMessage());
        }
        return "redirect:/admin/treatments";
    }

    @GetMapping("/treatment-image/{filename}")
    public ResponseEntity<Resource> getTreatmentImage(@PathVariable String filename) {
        try {
            Resource resource = fileStorageService.loadFileAsResource(filename);

            String contentType = "application/octet-stream";
            if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else if (filename.endsWith(".png")) {
                contentType = "image/png";
            } else if (filename.endsWith(".gif")) {
                contentType = "image/gif";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ==================== DOCTOR MANAGEMENT ====================

    @GetMapping("/doctors")
    public String listDoctors(Model model) {
        List<Doctor> doctors = doctorService.getAllDoctors();
        model.addAttribute("doctors", doctors);
        return "admin/doctors";
    }

    @GetMapping("/doctors/new")
    public String showDoctorForm(Model model) {
        model.addAttribute("doctor", new Doctor());
        return "admin/doctor-form";
    }

    @PostMapping("/doctors/save")
    public String saveDoctor(@ModelAttribute Doctor doctor,
                             @RequestParam(required = false) String password,
                             RedirectAttributes redirectAttributes) {
        try {
            if (doctor.getId() == null) {
                if (password == null || password.isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Password is required");
                    return "redirect:/admin/doctors/new";
                }
                doctorService.createDoctor(doctor, password);
                redirectAttributes.addFlashAttribute("success",
                        "Doctor created successfully. Username: " + doctor.getUsername());
            } else {
                doctorService.updateDoctor(doctor.getId(), doctor);
                redirectAttributes.addFlashAttribute("success", "Doctor updated successfully");
            }
            return "redirect:/admin/doctors";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/doctors/new";
        }
    }

    @GetMapping("/doctors/edit/{id}")
    public String editDoctor(@PathVariable Long id, Model model) {
        Doctor doctor = doctorService.getDoctorById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        model.addAttribute("doctor", doctor);
        return "admin/doctor-form";
    }

    @PostMapping("/doctors/delete/{id}")
    public String deleteDoctor(@PathVariable Long id,
                               RedirectAttributes redirectAttributes) {
        try {
            doctorService.deleteDoctor(id);
            redirectAttributes.addFlashAttribute("success", "Doctor deactivated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/doctors";
    }

    // ==================== APPOINTMENT MANAGEMENT ====================

    @GetMapping("/appointments")
    public String listAppointments(Model model) {
        List<Appointment> appointments = appointmentService.getAllAppointments();
        model.addAttribute("appointments", appointments);
        return "admin/appointments";
    }

    // ==================== ADMIN ACCOUNT MANAGEMENT ====================

    @GetMapping("/admins")
    public String listAdmins(Model model) {
        List<Admin> admins = adminService.getAllAdmins();
        model.addAttribute("admins", admins);
        return "admin/admins";
    }

    @GetMapping("/admins/new")
    public String showAdminForm(Model model) {
        model.addAttribute("admin", new Admin());
        return "admin/admin-form";
    }

    @PostMapping("/admins/save")
    public String saveAdmin(@ModelAttribute Admin admin,
                            @RequestParam String password,
                            @RequestParam(defaultValue = "false") Boolean isSuperAdmin,
                            RedirectAttributes redirectAttributes) {
        try {
            adminService.createAdmin(admin, password, isSuperAdmin);
            redirectAttributes.addFlashAttribute("success",
                    "Admin created successfully. Username: " + admin.getUsername());
            return "redirect:/admin/admins";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/admins/new";
        }
    }

    @PostMapping("/admins/deactivate/{id}")
    public String deactivateAdmin(@PathVariable Long id,
                                  RedirectAttributes redirectAttributes) {
        try {
            adminService.deactivateAdmin(id);
            redirectAttributes.addFlashAttribute("success", "Admin deactivated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/admins";
    }







    // ==================== PAYMENT MANAGEMENT ====================
    @GetMapping("/payments")
    public String listPayments(Model model) {
        try {
            List<Payment> payments = paymentService.getAllPayments();
            List<Payment> pendingVerification = paymentService.getPendingVerificationPayments();
            Map<String, Object> stats = paymentService.getPaymentStatistics();

            model.addAttribute("payments", payments);
            model.addAttribute("pendingVerification", pendingVerification);
            model.addAttribute("stats", stats);

            return "admin/payments";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading payments: " + e.getMessage());
            return "admin/payments";
        }
    }
    @GetMapping("/payments/pending-verification")
    public String viewPendingVerification(Model model) {
        try {
            List<Payment> payments = paymentService.getPendingVerificationPayments();
            model.addAttribute("payments", payments);
            System.out.println("Found " + payments.size() + " pending verification payments");
            return "admin/pending-verification";
        } catch (Exception e) {
            System.err.println("Error loading pending verifications: " + e.getMessage());
            model.addAttribute("error", "Error loading pending verifications: " + e.getMessage());
            return "admin/pending-verification";
        }
    }
    @PostMapping("/payments/verify/{paymentId}")
    public String verifyPayment(@PathVariable Long paymentId,
                                @RequestParam boolean approved,
                                @RequestParam(required = false) String reason,
                                Authentication auth,
                                RedirectAttributes redirectAttributes) {
        try {
            String verifiedBy = auth.getName();
            System.out.println("Admin verification called for payment ID: " + paymentId);

            Payment payment = paymentService.verifyReceiptUpload(paymentId, approved, verifiedBy, reason);

            if (approved) {
                redirectAttributes.addFlashAttribute("success",
                        "Payment verified successfully!");
            } else {
                redirectAttributes.addFlashAttribute("success",
                        "Payment rejected. Reason: " + reason);
            }
        } catch (Exception e) {
            System.err.println("Error during verification: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Verification failed: " + e.getMessage());
        }
        return "redirect:/admin/payments/pending-verification";
    }

    @PostMapping("/payments/refund/{paymentId}")
    public String processRefund(@PathVariable Long paymentId,
                                @RequestParam String refundReason,
                                RedirectAttributes redirectAttributes) {
        try {
            Payment payment = paymentService.refundPayment(paymentId, refundReason);
            redirectAttributes.addFlashAttribute("success",
                    "Refund processed: LKR " + payment.getRefundAmount());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/payments";
    }

    @PostMapping("/payments/delete/{paymentId}")
    public String deletePayment(@PathVariable Long paymentId,
                                RedirectAttributes redirectAttributes) {
        try {
            paymentService.deletePayment(paymentId);
            redirectAttributes.addFlashAttribute("success", "Payment deleted");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/payments";
    }
    @GetMapping("/payments/reports")
    public String showPaymentReports(Model model) {
        try {
            Map<String, Object> stats = paymentService.getPaymentStatistics();
            model.addAttribute("stats", stats);
            return "admin/payment-reports";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading reports: " + e.getMessage());
            return "admin/payment-reports";
        }
    }
    // ==================== PAYMENT REPORTS ====================

    // ==================== PAYMENT REPORTS - FIXED (Cash & Receipt Only) ====================

    @GetMapping("/payments/reports/daily")
    public String dailyReports(@RequestParam(required = false) String date, Model model) {
        try {
            LocalDate selectedDate = (date != null) ? LocalDate.parse(date) : LocalDate.now();
            Map<String, Object> summary = paymentService.getDailySummary(selectedDate);

            @SuppressWarnings("unchecked")
            List<Payment> payments = (List<Payment>) summary.get("payments");

            long completedPayments = payments.stream()
                    .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                    .count();

            Double totalRefunds = payments.stream()
                    .filter(p -> p.getStatus() == PaymentStatus.REFUNDED)
                    .mapToDouble(p -> p.getRefundAmount() != null ? p.getRefundAmount() : 0.0)
                    .sum();

            Double totalRevenue = (Double) summary.get("totalRevenue");
            Double netRevenue = totalRevenue - totalRefunds;

            summary.put("completedPayments", completedPayments);
            summary.put("totalRefunds", totalRefunds);
            summary.put("netRevenue", netRevenue);

            model.addAttribute("summary", summary);
            model.addAttribute("selectedDate", selectedDate);

            System.out.println("Returning template: admin/daily-reports");
            return "admin/daily-reports";
        } catch (Exception e) {
            System.err.println("Error in dailyReports: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error loading daily report: " + e.getMessage());
            return "admin/daily-reports";
        }
    }

    @GetMapping("/payments/reports/monthly")
    public String monthlyReports(@RequestParam(required = false) Integer year,
                                 @RequestParam(required = false) Integer month,
                                 Model model) {
        try {
            LocalDate now = LocalDate.now();
            int selectedYear = (year != null) ? year : now.getYear();
            int selectedMonth = (month != null) ? month : now.getMonthValue();

            Map<String, Object> summary = paymentService.getMonthlySummary(selectedYear, selectedMonth);

            @SuppressWarnings("unchecked")
            List<Payment> payments = (List<Payment>) summary.get("payments");

            // Only Cash and Receipt Upload methods
            long cashPayments = payments.stream()
                    .filter(p -> p.getPaymentMethod() == PaymentMethod.CASH)
                    .count();
            long receiptPayments = payments.stream()
                    .filter(p -> p.getPaymentMethod() == PaymentMethod.RECEIPT_UPLOAD)
                    .count();
            long refunded = payments.stream()
                    .filter(p -> p.getStatus() == PaymentStatus.REFUNDED)
                    .count();

            summary.put("cashPayments", cashPayments);
            summary.put("receiptPayments", receiptPayments);
            summary.put("refunded", refunded);

            Integer totalPayments = (Integer) summary.get("totalPayments");
            Double totalRevenue = (Double) summary.get("totalRevenue");
            Double averagePayment = totalPayments > 0 ? totalRevenue / totalPayments : 0.0;
            summary.put("averagePayment", averagePayment);

            model.addAttribute("summary", summary);
            model.addAttribute("selectedYear", selectedYear);
            model.addAttribute("selectedMonth", selectedMonth);

            System.out.println("Returning template: admin/monthly-reports");
            return "admin/monthly-reports";
        } catch (Exception e) {
            System.err.println("Error in monthlyReports: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error loading monthly report: " + e.getMessage());
            return "admin/monthly-reports";
        }
    }
    @GetMapping("/payments/reports/export-daily")
    public ResponseEntity<String> exportDailyReport(@RequestParam String date) {
        LocalDate selectedDate = LocalDate.parse(date);
        String csv = paymentService.exportPaymentsToCSV(selectedDate, selectedDate);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=daily-report-" + date + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/payments/reports/export-monthly")
    public ResponseEntity<String> exportMonthlyReport(@RequestParam int year, @RequestParam int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        String csv = paymentService.exportPaymentsToCSV(startDate, endDate);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=monthly-report-" + year + "-" + month + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/payments/export/csv")
    public ResponseEntity<String> exportPaymentsCSV(@RequestParam String startDate,
                                                    @RequestParam String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        String csv = paymentService.exportPaymentsToCSV(start, end);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=payments-" + startDate + "-to-" + endDate + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }


    @GetMapping("/payments/receipt/view/{paymentId}")
    public ResponseEntity<Resource> viewUploadedReceipt(@PathVariable Long paymentId,
                                                        Authentication auth) {
        try {
            Payment payment = paymentService.getPaymentById(paymentId)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            if (payment.getReceiptFileName() == null) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = fileStorageService.loadFileAsResource(payment.getReceiptFileName());

            String contentType = "application/octet-stream";
            String fileName = payment.getReceiptFileName();
            if (fileName.endsWith(".pdf")) {
                contentType = "application/pdf";
            } else if (fileName.matches(".*\\.(jpg|jpeg)$")) {
                contentType = "image/jpeg";
            } else if (fileName.endsWith(".png")) {
                contentType = "image/png";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                    .body(resource);

        } catch (Exception e) {
            System.err.println("Error loading receipt: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    // ==================== ADMIN CHARGES & EARNINGS MANAGEMENT ====================

    @GetMapping("/charges")
    public String viewAdminCharges(Model model) {
        try {
            List<AdminCharge> charges = doctorEarningService.getAllAdminCharges();
            Double totalCharges = doctorEarningService.getTotalAdminCharges();

            // Calculate monthly breakdown
            LocalDate now = LocalDate.now();
            LocalDate startOfMonth = now.withDayOfMonth(1);
            Double monthlyCharges = doctorEarningService.getAdminChargesByDateRange(startOfMonth, now);

            model.addAttribute("charges", charges);
            model.addAttribute("totalCharges", totalCharges != null ? totalCharges : 0.0);
            model.addAttribute("monthlyCharges", monthlyCharges != null ? monthlyCharges : 0.0);

            return "admin/admin-charges";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading charges: " + e.getMessage());
            return "admin/admin-charges";
        }
    }

    @GetMapping("/earnings/doctor/{doctorId}")
    public String viewDoctorEarnings(@PathVariable Long doctorId, Model model) {
        try {
            Doctor doctor = doctorService.getDoctorById(doctorId)
                    .orElseThrow(() -> new RuntimeException("Doctor not found"));

            Map<String, Object> earningSummary = doctorEarningService.getDoctorEarningSummary(doctorId);
            List<DoctorEarning> allEarnings = doctorEarningService.getDoctorEarnings(doctorId);

            model.addAttribute("doctor", doctor);
            model.addAttribute("earningSummary", earningSummary);
            model.addAttribute("allEarnings", allEarnings);

            return "admin/doctor-earnings";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading earnings: " + e.getMessage());
            return "admin/doctor-earnings";
        }
    }

    @GetMapping("/earnings/all-doctors")
    public String viewAllDoctorEarnings(Model model) {
        try {
            List<Doctor> doctors = doctorService.getAllDoctors();
            Map<Long, Double> doctorTotalEarnings = new HashMap<>();
            Map<Long, Double> doctorPendingEarnings = new HashMap<>();

            for (Doctor doctor : doctors) {
                Double total = doctorEarningService.getTotalDoctorEarnings(doctor.getId());
                Double pending = doctorEarningService.getPendingEarningsAmount(doctor.getId());

                doctorTotalEarnings.put(doctor.getId(), total != null ? total : 0.0);
                doctorPendingEarnings.put(doctor.getId(), pending != null ? pending : 0.0);
            }

            model.addAttribute("doctors", doctors);
            model.addAttribute("doctorTotalEarnings", doctorTotalEarnings);
            model.addAttribute("doctorPendingEarnings", doctorPendingEarnings);

            return "admin/all-doctors-earnings";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading earnings: " + e.getMessage());
            return "admin/all-doctors-earnings";
        }
    }

    @PostMapping("/earnings/settle/{earningId}")
    public String settleEarning(@PathVariable Long earningId,
                                @RequestParam String settlementReference,
                                @RequestParam(required = false) String notes,
                                RedirectAttributes redirectAttributes) {
        try {
            DoctorEarning earning = doctorEarningService.settleEarning(earningId, settlementReference, notes);
            redirectAttributes.addFlashAttribute("success",
                    "Earning settled successfully. Amount: LKR " + earning.getNetEarning());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error settling earning: " + e.getMessage());
        }
        return "redirect:/admin/earnings/all-doctors";
    }

    @GetMapping("/financial-summary")
    public String viewFinancialSummary(Model model) {
        try {
            // Get payment statistics
            Map<String, Object> paymentStats = paymentService.getPaymentStatistics();

            // Get admin charges
            Double totalAdminCharges = doctorEarningService.getTotalAdminCharges();

            // Calculate total doctor earnings across all doctors
            List<Doctor> doctors = doctorService.getAllDoctors();
            Double totalDoctorEarnings = 0.0;
            Double totalPendingSettlements = 0.0;

            for (Doctor doctor : doctors) {
                Double doctorTotal = doctorEarningService.getTotalDoctorEarnings(doctor.getId());
                Double doctorPending = doctorEarningService.getPendingEarningsAmount(doctor.getId());

                totalDoctorEarnings += (doctorTotal != null ? doctorTotal : 0.0);
                totalPendingSettlements += (doctorPending != null ? doctorPending : 0.0);
            }

            model.addAttribute("paymentStats", paymentStats);
            model.addAttribute("totalAdminCharges", totalAdminCharges != null ? totalAdminCharges : 0.0);
            model.addAttribute("totalDoctorEarnings", totalDoctorEarnings);
            model.addAttribute("totalPendingSettlements", totalPendingSettlements);
            model.addAttribute("totalDoctors", doctors.size());

            return "admin/financial-summary";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading financial summary: " + e.getMessage());
            return "admin/financial-summary";
        }
    }
}