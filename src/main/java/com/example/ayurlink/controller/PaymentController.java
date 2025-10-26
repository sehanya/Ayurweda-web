
package com.example.ayurlink.controller;

import com.example.ayurlink.model.*;
import com.example.ayurlink.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
        import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.util.Map;

@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final AppointmentService appointmentService;
    private final PatientService patientService;
    private final FileStorageService fileStorageService;

    // ==================== PATIENT PAYMENT ROUTES ====================

    @GetMapping("/create/{appointmentId}")
    public String showPaymentForm(@PathVariable("appointmentId") Long appointmentId,
                                  Authentication auth,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        try {
            System.out.println("=== PaymentController.showPaymentForm called ===");
            System.out.println("Appointment ID: " + appointmentId);

            String username = auth.getName();
            Patient patient = patientService.getPatientByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Patient not found"));

            Appointment appointment = appointmentService.getAppointmentById(appointmentId)
                    .orElseThrow(() -> new RuntimeException("Appointment not found"));

            if (!appointment.getPatient().getId().equals(patient.getId())) {
                redirectAttributes.addFlashAttribute("error", "Unauthorized access");
                return "redirect:/patient/appointments";
            }

            if (paymentService.getPaymentByAppointmentId(appointmentId).isPresent()) {
                Payment existingPayment = paymentService.getPaymentByAppointmentId(appointmentId).get();

                // Allow retry if payment was rejected
                if (existingPayment.getStatus() != PaymentStatus.REJECTED &&
                        existingPayment.getStatus() != PaymentStatus.FAILED) {
                    redirectAttributes.addFlashAttribute("error", "Payment already exists for this appointment");
                    return "redirect:/patient/appointments";
                }
            }

            Map<String, Double> breakdown = paymentService.calculatePaymentBreakdown(appointmentId);
            // Add bank account details to model
            Map<String, String> bankDetails = paymentService.getBankAccountDetails();
            model.addAttribute("appointment", appointment);
            model.addAttribute("breakdown", breakdown);
            model.addAttribute("bankDetails", bankDetails);

            System.out.println("Returning payment form view");
            return "patient/payment-form";

        } catch (Exception e) {
            System.err.println("Error in showPaymentForm: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/patient/appointments";
        }
    }

    @PostMapping("/process")
    public String processPayment(@RequestParam Long appointmentId,
                                 @RequestParam String paymentMethod,
                                 @RequestParam(required = false) MultipartFile receiptFile,
                                 @RequestParam(required = false) String notes,
                                 Authentication auth,
                                 RedirectAttributes redirectAttributes) {
        try {
            System.out.println("=== PaymentController.processPayment called ===");
            System.out.println("Payment Method: " + paymentMethod);

            String username = auth.getName();
            Patient patient = patientService.getPatientByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Patient not found"));

            Appointment appointment = appointmentService.getAppointmentById(appointmentId)
                    .orElseThrow(() -> new RuntimeException("Appointment not found"));

            if (!appointment.getPatient().getId().equals(patient.getId())) {
                redirectAttributes.addFlashAttribute("error", "Unauthorized access");
                return "redirect:/patient/appointments";
            }

            Payment payment;
            PaymentMethod method = PaymentMethod.valueOf(paymentMethod.toUpperCase());

            switch (method) {
                case CASH:
                    payment = paymentService.createCashPayment(appointmentId, notes);
                    redirectAttributes.addFlashAttribute("success",
                            "Cash payment recorded! Please visit clinic. Receipt: " + payment.getReceiptNumber());
                    break;

                case RECEIPT_UPLOAD:
                    if (receiptFile == null || receiptFile.isEmpty()) {
                        redirectAttributes.addFlashAttribute("error", "Please upload receipt file");
                        return "redirect:/payment/create/" + appointmentId;
                    }
                    payment = paymentService.createReceiptUploadPayment(appointmentId, receiptFile, notes);
                    redirectAttributes.addFlashAttribute("success",
                            "Receipt uploaded! Awaiting verification. Receipt: " + payment.getReceiptNumber());
                    System.out.println("Receipt upload payment created with status: " + payment.getStatus());
                    break;

                default:
                    throw new RuntimeException("Invalid payment method");
            }

            return "redirect:/payment/receipt/" + payment.getId();

        } catch (Exception e) {
            System.err.println("Error in processPayment: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Payment failed: " + e.getMessage());
            return "redirect:/payment/create/" + appointmentId;
        }
    }

    @GetMapping("/receipt/{paymentId}")
    public String viewReceipt(@PathVariable("paymentId") Long paymentId,
                              Authentication auth,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        try {
            System.out.println("=== PaymentController.viewReceipt called ===");

            String username = auth.getName();
            Patient patient = patientService.getPatientByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Patient not found"));

            Payment payment = paymentService.getPaymentById(paymentId)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            if (!payment.getAppointment().getPatient().getId().equals(patient.getId())) {
                redirectAttributes.addFlashAttribute("error", "Unauthorized access");
                return "redirect:/patient/payments";
            }

            Map<String, Object> receipt = paymentService.getPaymentReceipt(paymentId);
            model.addAttribute("receipt", receipt);
            model.addAttribute("payment", payment);

            // Display payment status message
            String statusMessage = getPaymentStatusMessage(payment);
            model.addAttribute("statusMessage", statusMessage);
            return "patient/payment-receipt";

        } catch (Exception e) {
            System.err.println("Error in viewReceipt: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/patient/payments";
        }
    }

    @GetMapping("/receipt/download/{paymentId}")
    public ResponseEntity<Resource> downloadReceipt(@PathVariable Long paymentId,
                                                    Authentication auth) {

        System.out.println("===== downloadReceipt called with paymentId: " + paymentId + " =====");

        try {
            System.out.println("=== downloadReceipt called with paymentId: " + paymentId + " ===");

            Payment payment = paymentService.getPaymentById(paymentId)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            // Check authorization
            String username = auth.getName();
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"));

            if (!isAdmin) {
                // Only non-admin users need to be the payment owner
                Patient patient = patientService.getPatientByUsername(username)
                        .orElseThrow(() -> new RuntimeException("Patient not found"));

                if (!payment.getAppointment().getPatient().getId().equals(patient.getId())) {
                    return ResponseEntity.status(403).build();
                }
            }

            // Admins can view any receipt

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
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(resource);

        } catch (Exception e) {
            System.err.println("Error downloading receipt: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }
    // Helper method to get payment status message
    private String getPaymentStatusMessage(Payment payment) {
        switch (payment.getStatus()) {
            case PENDING:
                return "Payment is pending. Cash payment waiting at clinic.";
            case PENDING_VERIFICATION:
                return "Receipt uploaded! Awaiting admin verification...";
            case SUCCESS:
                return "Payment confirmed and verified!";
            case COMPLETED:
                return "Payment completed successfully!";
            case REJECTED:
                return "Receipt rejected. Please upload again.";
            case REFUNDED:
                return "Payment has been refunded.";
            case FAILED:
                return "Payment failed. Please try again.";
            case CANCELLED:
                return "Payment cancelled.";
            default:
                return "Payment status: " + payment.getStatus();
        }
    }
}





