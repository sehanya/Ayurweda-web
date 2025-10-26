package com.example.ayurlink.service;

import com.example.ayurlink.model.*;
import com.example.ayurlink.repository.AppointmentRepository;
import com.example.ayurlink.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;
    private final FileStorageService fileStorageService;
    private final DoctorEarningService doctorEarningService;

    private static final Double DEFAULT_CLINIC_CHARGES = 500.0;

    // ==================== BANK ACCOUNT DETAILS ====================

    public Map<String, String> getBankAccountDetails() {
        Map<String, String> bankDetails = new HashMap<>();
        bankDetails.put("bankName", "Commercial Bank");
        bankDetails.put("accountName", "Ayur-Link Ayurvedic Clinic");
        bankDetails.put("accountNumber", "8560123456789");
        bankDetails.put("branch", "Colombo 07");
        bankDetails.put("swiftCode", "CCEYLKLX");
        return bankDetails;
    }
    // ==================== CREATE OPERATIONS ====================

    public Payment createCashPayment(Long appointmentId, String notes) {
        log.info("Creating cash payment for appointment ID: {}", appointmentId);
        Appointment appointment = validateAppointment(appointmentId);
        Payment payment = buildPayment(appointment, PaymentMethod.CASH, notes);
// Cash payment is immediately successful
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaymentDate(LocalDateTime.now());

        // Confirm the appointment
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointmentRepository.save(appointment);

        // Save payment to database
        Payment savedPayment = paymentRepository.save(payment);
        log.info("Cash payment saved with ID: {}, Status: SUCCESS", savedPayment.getId());
        return paymentRepository.save(payment);
    }

    public Payment createReceiptUploadPayment(Long appointmentId, MultipartFile receiptFile, String notes) {
        log.info("Creating receipt upload payment for appointment ID: {}", appointmentId);
        Appointment appointment = validateAppointment(appointmentId);

        if (receiptFile == null || receiptFile.isEmpty()) {
            throw new RuntimeException("Receipt file is required");
        }

        if (!isValidReceiptFile(receiptFile.getContentType())) {
            throw new RuntimeException("Invalid file type. Only JPG, PNG, and PDF allowed.");
        }
//store the file
        String fileName = fileStorageService.storeFile(receiptFile, "receipt_" + appointmentId);
        log.info("Receipt file stored successfully: {}", fileName);

//build payment object
        Payment payment = buildPayment(appointment, PaymentMethod.RECEIPT_UPLOAD, notes);


        payment.setReceiptFileName(fileName);
        payment.setReceiptFilePath("uploads/receipts/" + fileName);
        payment.setReceiptUploadDate(LocalDateTime.now());
        payment.setStatus(PaymentStatus.PENDING_VERIFICATION);
        payment.setReceiptVerified(false);
        payment.setPaymentDate(LocalDateTime.now());

        log.info("Payment object created with receipt details");

        // Set appointment to PENDING until admin verifies
        appointment.setStatus(AppointmentStatus.PENDING);
        appointmentRepository.save(appointment);
        log.info("Appointment status set to PENDING");

        // Save payment to database
        Payment savedPayment = paymentRepository.save(payment);

        log.info("=== PAYMENT SAVED TO DATABASE ===");
        log.info("Payment ID: {}", savedPayment.getId());
        log.info("Receipt Number: {}", savedPayment.getReceiptNumber());
        log.info("Transaction ID: {}", savedPayment.getTransactionId());
        log.info("Status: {}", savedPayment.getStatus());
        log.info("Receipt File: {}", savedPayment.getReceiptFileName());
        log.info("Receipt Path: {}", savedPayment.getReceiptFilePath());
        log.info("Upload Date: {}", savedPayment.getReceiptUploadDate());
        log.info("================================");


        return paymentRepository.save(payment);
    }



    // ==================== READ OPERATIONS ====================

    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }

    public Optional<Payment> getPaymentByAppointmentId(Long appointmentId) {
        return paymentRepository.findByAppointmentId(appointmentId);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public List<Payment> getPatientPayments(Long patientId) {
        return paymentRepository.findByAppointment_Patient_Id(patientId);
    }

    public List<Payment> getPendingVerificationPayments() {
        return paymentRepository.findByStatusOrderByPaymentDateDesc(PaymentStatus.PENDING_VERIFICATION);
    }

    public List<Payment> getPaymentsByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        return paymentRepository.findByPaymentDateBetweenOrderByPaymentDateDesc(start, end);
    }

    // ==================== UPDATE OPERATIONS ====================


    public Payment verifyReceiptUpload(Long paymentId, boolean approved, String verifiedBy, String reason) {
        log.info("=== STARTING RECEIPT VERIFICATION ===");
        log.info("Payment ID: {}", paymentId);
        log.info("Approved: {}", approved);
        log.info("Verified by: {}", verifiedBy);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getPaymentMethod() != PaymentMethod.RECEIPT_UPLOAD) {
            throw new RuntimeException("Not a receipt upload payment");
        }
        if (payment.getStatus() != PaymentStatus.PENDING_VERIFICATION) {
            throw new RuntimeException("Payment is not pending verification. Current status: " + payment.getStatus());
        }
        Appointment appointment = payment.getAppointment();
        log.info("Associated appointment: {}", appointment.getTicketNumber());

        if (approved) {
            // APPROVE: Payment successful, appointment confirmed
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setReceiptVerified(true);
            payment.setVerifiedBy(verifiedBy);
            payment.setVerificationDate(LocalDateTime.now());

            appointment.setStatus(AppointmentStatus.CONFIRMED);

            log.info("✅ APPROVED - Payment: SUCCESS, Appointment: CONFIRMED");
            try {
                doctorEarningService.createEarningRecords(payment);
                log.info("Earning records created successfully for payment: {}", payment.getId());
            } catch (Exception e) {
                log.error("Error creating earning records: {}", e.getMessage());
                // Continue even if earning record creation fails
            }

        } else {
            // REJECT: Payment rejected, appointment stays pending
            payment.setStatus(PaymentStatus.REJECTED);
            payment.setReceiptVerified(false);
            payment.setVerifiedBy(verifiedBy);
            payment.setVerificationDate(LocalDateTime.now());

            // Append rejection reason to notes
            String currentNotes = payment.getPaymentNotes() != null ? payment.getPaymentNotes() : "";
            String rejectionNote = " | REJECTED by " + verifiedBy + " on " + LocalDateTime.now() +
                    ": " + (reason != null ? reason : "No reason provided");
            payment.setPaymentNotes(currentNotes + rejectionNote);

            // Keep appointment as PENDING so patient can retry payment
            appointment.setStatus(AppointmentStatus.PENDING);

            log.info("❌ REJECTED - Payment: REJECTED, Appointment: PENDING, Reason: {}", reason);
        }
        // Save appointment
        Appointment savedAppointment = appointmentRepository.save(appointment);
        log.info("Appointment saved - Status: {}", savedAppointment.getStatus());

        // Save payment
        Payment savedPayment = paymentRepository.save(payment);

        log.info("=== VERIFICATION SAVED TO DATABASE ===");
        log.info("Payment ID: {}", savedPayment.getId());
        log.info("Payment Status: {}", savedPayment.getStatus());
        log.info("Receipt Verified: {}", savedPayment.getReceiptVerified());
        log.info("Verified By: {}", savedPayment.getVerifiedBy());
        log.info("Verification Date: {}", savedPayment.getVerificationDate());
        log.info("Appointment Status: {}", savedAppointment.getStatus());
        log.info("====================================");

        return paymentRepository.save(payment);
    }

    public Payment refundPayment(Long paymentId, String refundReason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new RuntimeException("Only successful payments can be refunded");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundAmount(payment.getTotalAmount());
        payment.setRefundDate(LocalDateTime.now());
        payment.setRefundReason(refundReason);

        Appointment appointment = payment.getAppointment();
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
        // Cancel earning records when payment is refunded
        try {
            doctorEarningService.handleRefund(payment);
            log.info("Earning records cancelled for refunded payment: {}", payment.getId());
        } catch (Exception e) {
            log.error("Error cancelling earning records: {}", e.getMessage());
        }

        return paymentRepository.save(payment);
    }

    // ==================== DELETE OPERATIONS ====================

    public void deletePayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            throw new RuntimeException("Cannot delete successful payment. Use refund.");
        }

        // Delete uploaded receipt file if exists
        if (payment.getReceiptFileName() != null) {
            fileStorageService.deleteFile(payment.getReceiptFileName());
        }

        paymentRepository.delete(payment);
        log.info("Payment deleted: {}", paymentId);
    }

    // ==================== CALCULATION & BREAKDOWN ====================

    public Map<String, Double> calculatePaymentBreakdown(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        Double doctorFee = appointment.getDoctor().getConsultationFee();
        Double treatmentFee = appointment.getTreatment().getCost();
        Double total = doctorFee + treatmentFee + DEFAULT_CLINIC_CHARGES;

        Map<String, Double> breakdown = new HashMap<>();
        breakdown.put("doctorFee", doctorFee);
        breakdown.put("treatmentFee", treatmentFee);
        breakdown.put("clinicCharges", DEFAULT_CLINIC_CHARGES);
        breakdown.put("totalAmount", total);
        return breakdown;
    }

    public Map<String, Object> getPaymentReceipt(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        Map<String, Object> receipt = new HashMap<>();
        receipt.put("payment", payment);
        receipt.put("receiptNumber", payment.getReceiptNumber());
        receipt.put("transactionId", payment.getTransactionId());
        receipt.put("patientName", payment.getAppointment().getPatient().getFullName());
        receipt.put("patientNIC", payment.getAppointment().getPatient().getNic());
        receipt.put("doctorName", payment.getAppointment().getDoctor().getFullName());
        receipt.put("treatmentName", payment.getAppointment().getTreatment().getName());
        receipt.put("appointmentDate", payment.getAppointment().getAppointmentDate());
        receipt.put("appointmentTime", payment.getAppointment().getAppointmentTime());
        return receipt;
    }

    // ==================== REPORTS & STATISTICS ====================

    public Map<String, Object> getDailySummary(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        List<Payment> payments = paymentRepository.findByPaymentDateBetween(start, end);

        Double totalRevenue = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .mapToDouble(Payment::getTotalAmount)
                .sum();

        Map<String, Object> summary = new HashMap<>();
        summary.put("date", date);
        summary.put("totalPayments", payments.size());
        summary.put("totalRevenue", totalRevenue);
        summary.put("payments", payments);
        return summary;
    }

    public Map<String, Object> getMonthlySummary(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<Payment> payments = paymentRepository.findByPaymentDateBetween(start, end);

        Double totalRevenue = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .mapToDouble(Payment::getTotalAmount)
                .sum();

        Double totalRefunds = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.REFUNDED)
                .mapToDouble(p -> p.getRefundAmount() != null ? p.getRefundAmount() : 0.0)
                .sum();

        Map<String, Object> summary = new HashMap<>();
        summary.put("year", year);
        summary.put("month", month);
        summary.put("totalPayments", payments.size());
        summary.put("totalRevenue", totalRevenue);
        summary.put("totalRefunds", totalRefunds);
        summary.put("netRevenue", totalRevenue - totalRefunds);
        summary.put("payments", payments);
        return summary;
    }

    public Map<String, Object> getPaymentStatistics() {
        List<Payment> allPayments = paymentRepository.findAll();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPayments", allPayments.size());
        stats.put("successfulPayments", allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS).count());
        stats.put("pendingVerification", allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING_VERIFICATION).count());
        stats.put("pendingCOD", allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING).count());
        stats.put("refunded", allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.REFUNDED).count());

        Double totalRevenue = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .mapToDouble(Payment::getTotalAmount).sum();

        Double totalRefunds = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.REFUNDED)
                .mapToDouble(p -> p.getRefundAmount() != null ? p.getRefundAmount() : 0.0).sum();

        stats.put("totalRevenue", totalRevenue);
        stats.put("totalRefunds", totalRefunds);
        stats.put("netRevenue", totalRevenue - totalRefunds);

        stats.put("cashPayments", allPayments.stream()
                .filter(p -> p.getPaymentMethod() == PaymentMethod.CASH).count());
        stats.put("receiptPayments", allPayments.stream()
                .filter(p -> p.getPaymentMethod() == PaymentMethod.RECEIPT_UPLOAD).count());


        return stats;
    }

    public String exportPaymentsToCSV(LocalDate startDate, LocalDate endDate) {
        List<Payment> payments = getPaymentsByDateRange(startDate, endDate);
        StringBuilder csv = new StringBuilder();
        csv.append("Receipt,Transaction,Patient,NIC,Doctor,Treatment,Amount,Method,Status,Date\n");

        for (Payment p : payments) {
            csv.append(p.getReceiptNumber()).append(",")
                    .append(p.getTransactionId()).append(",")
                    .append(p.getAppointment().getPatient().getFullName()).append(",")
                    .append(p.getAppointment().getPatient().getNic()).append(",")
                    .append(p.getAppointment().getDoctor().getFullName()).append(",")
                    .append(p.getAppointment().getTreatment().getName()).append(",")
                    .append(p.getTotalAmount()).append(",")
                    .append(p.getPaymentMethod()).append(",")
                    .append(p.getStatus()).append(",")
                    .append(p.getPaymentDate()).append("\n");
        }
        return csv.toString();
    }

    public Map<String, Object> generateDetailedReport(LocalDate startDate, LocalDate endDate) {
        List<Payment> payments = getPaymentsByDateRange(startDate, endDate);

        Map<String, Object> report = new HashMap<>();
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("totalPayments", payments.size());

        // Revenue breakdown
        Double totalRevenue = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .mapToDouble(Payment::getTotalAmount).sum();

        Double doctorFees = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .mapToDouble(Payment::getDoctorFee).sum();

        Double treatmentFees = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .mapToDouble(Payment::getTreatmentFee).sum();

        Double clinicCharges = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .mapToDouble(Payment::getClinicCharges).sum();

        report.put("totalRevenue", totalRevenue);
        report.put("doctorFees", doctorFees);
        report.put("treatmentFees", treatmentFees);
        report.put("clinicCharges", clinicCharges);

        // Payment method breakdown
        Map<PaymentMethod, Long> methodBreakdown = payments.stream()
                .collect(Collectors.groupingBy(Payment::getPaymentMethod, Collectors.counting()));
        report.put("methodBreakdown", methodBreakdown);

        // Status breakdown
        Map<PaymentStatus, Long> statusBreakdown = payments.stream()
                .collect(Collectors.groupingBy(Payment::getStatus, Collectors.counting()));
        report.put("statusBreakdown", statusBreakdown);

        // Top treatments
        Map<String, Long> treatmentStats = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .collect(Collectors.groupingBy(
                        p -> p.getAppointment().getTreatment().getName(),
                        Collectors.counting()
                ));
        report.put("topTreatments", treatmentStats);

        // Top doctors
        Map<String, Long> doctorStats = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .collect(Collectors.groupingBy(
                        p -> p.getAppointment().getDoctor().getFullName(),
                        Collectors.counting()
                ));
        report.put("topDoctors", doctorStats);

        report.put("payments", payments);
        return report;
    }

    // ==================== HELPER METHODS ====================

    private Appointment validateAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new RuntimeException("Cannot process payment for cancelled appointment");
        }

        Optional<Payment> existing = paymentRepository.findByAppointmentId(appointmentId);
        if (existing.isPresent() &&
                existing.get().getStatus() != PaymentStatus.REJECTED &&
                existing.get().getStatus() != PaymentStatus.FAILED) {
            throw new RuntimeException("Payment already exists");
        }

        return appointment;
    }

    private Payment buildPayment(Appointment appointment, PaymentMethod method, String notes) {
        Payment payment = new Payment();
        payment.setAppointment(appointment);
        payment.setDoctorFee(appointment.getDoctor().getConsultationFee());
        payment.setTreatmentFee(appointment.getTreatment().getCost());
        payment.setClinicCharges(DEFAULT_CLINIC_CHARGES);
        payment.calculateTotal();
        payment.setPaymentMethod(method);
        payment.setPaymentNotes(notes);
        return payment;
    }

    private boolean isValidReceiptFile(String contentType) {
        return contentType != null && (
                contentType.equals("image/jpeg") ||
                        contentType.equals("image/jpg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("application/pdf")
        );
    }

    public Payment getPaymentByAppointment(Long appointmentId) {
        return paymentRepository.findByAppointmentId(appointmentId)
                .orElse(null);
    }

// Add these methods to your PaymentService class (after the existing generateDetailedReport method)

// ==================== ADDITIONAL REPORT METHODS ====================

    public Map<String, Object> getPaymentMethodAnalysis() {
        List<Payment> allPayments = paymentRepository.findAll();

        Map<String, Object> analysis = new HashMap<>();

        // Count by payment method
        Map<PaymentMethod, Long> methodCounts = allPayments.stream()
                .collect(Collectors.groupingBy(Payment::getPaymentMethod, Collectors.counting()));

        // Revenue by payment method
        Map<PaymentMethod, Double> methodRevenue = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .collect(Collectors.groupingBy(
                        Payment::getPaymentMethod,
                        Collectors.summingDouble(Payment::getTotalAmount)
                ));

        analysis.put("methodCounts", methodCounts);
        analysis.put("methodRevenue", methodRevenue);
        analysis.put("totalPayments", allPayments.size());

        return analysis;
    }

    public Map<String, Object> getDoctorPerformanceReport() {
        List<Payment> successfulPayments = paymentRepository.findByStatus(PaymentStatus.SUCCESS);

        Map<String, Object> report = new HashMap<>();

        // Group by doctor
        Map<String, List<Payment>> paymentsByDoctor = successfulPayments.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getAppointment().getDoctor().getFullName()
                ));

        // Calculate stats per doctor
        Map<String, Map<String, Object>> doctorStats = new HashMap<>();
        for (Map.Entry<String, List<Payment>> entry : paymentsByDoctor.entrySet()) {
            String doctorName = entry.getKey();
            List<Payment> payments = entry.getValue();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalAppointments", payments.size());
            stats.put("totalRevenue", payments.stream()
                    .mapToDouble(Payment::getTotalAmount).sum());
            stats.put("doctorFees", payments.stream()
                    .mapToDouble(Payment::getDoctorFee).sum());

            doctorStats.put(doctorName, stats);
        }

        report.put("doctorStats", doctorStats);
        report.put("totalDoctors", doctorStats.size());

        return report;
    }

    public Map<String, Object> getTreatmentAnalysis() {
        List<Payment> successfulPayments = paymentRepository.findByStatus(PaymentStatus.SUCCESS);

        Map<String, Object> analysis = new HashMap<>();

        // Group by treatment
        Map<String, List<Payment>> paymentsByTreatment = successfulPayments.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getAppointment().getTreatment().getName()
                ));



        // Calculate stats per treatment
        Map<String, Map<String, Object>> treatmentStats = new HashMap<>();
        for (Map.Entry<String, List<Payment>> entry : paymentsByTreatment.entrySet()) {
            String treatmentName = entry.getKey();
            List<Payment> payments = entry.getValue();

            Map<String, Object> stats = new HashMap<>();
            stats.put("count", payments.size());
            stats.put("totalRevenue", payments.stream()
                    .mapToDouble(Payment::getTotalAmount).sum());
            stats.put("averageRevenue", payments.stream()
                    .mapToDouble(Payment::getTotalAmount).average().orElse(0.0));

            treatmentStats.put(treatmentName, stats);
        }

        analysis.put("treatmentStats", treatmentStats);
        analysis.put("totalTreatments", treatmentStats.size());

        return analysis;
    }

    public Map<String, Object> getRefundReport() {
        List<Payment> refundedPayments = paymentRepository.findByStatus(PaymentStatus.REFUNDED);

        Map<String, Object> report = new HashMap<>();

        Double totalRefundAmount = refundedPayments.stream()
                .mapToDouble(p -> p.getRefundAmount() != null ? p.getRefundAmount() : 0.0)
                .sum();

        // Group by refund reason
        Map<String, Long> refundReasons = refundedPayments.stream()
                .filter(p -> p.getRefundReason() != null)
                .collect(Collectors.groupingBy(
                        Payment::getRefundReason,
                        Collectors.counting()
                ));

        report.put("totalRefunds", refundedPayments.size());
        report.put("totalRefundAmount", totalRefundAmount);
        report.put("refundReasons", refundReasons);
        report.put("refundedPayments", refundedPayments);

        return report;
    }

    public Map<String, Object> getPatientAnalytics() {
        List<Payment> allPayments = paymentRepository.findAll();

        Map<String, Object> analytics = new HashMap<>();

        // Unique patients
        Set<Long> uniquePatients = allPayments.stream()
                .map(p -> p.getAppointment().getPatient().getId())
                .collect(Collectors.toSet());

        // Patient payment frequency
        Map<String, Long> patientFrequency = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .collect(Collectors.groupingBy(
                        p -> p.getAppointment().getPatient().getFullName(),
                        Collectors.counting()
                ));

        // Patient total spending
        Map<String, Double> patientSpending = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .collect(Collectors.groupingBy(
                        p -> p.getAppointment().getPatient().getFullName(),
                        Collectors.summingDouble(Payment::getTotalAmount)
                ));

        analytics.put("totalPatients", uniquePatients.size());
        analytics.put("patientFrequency", patientFrequency);
        analytics.put("patientSpending", patientSpending);

        return analytics;
    }

    public Map<String, Object> getSystemOverview() {
        Map<String, Object> overview = new HashMap<>();

        // Get all payments
        List<Payment> allPayments = paymentRepository.findAll();
        List<Appointment> allAppointments = appointmentRepository.findAll();

        // Payment statistics
        overview.put("totalPayments", allPayments.size());
        overview.put("successfulPayments", allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS).count());
        overview.put("pendingPayments", allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING_VERIFICATION).count());

        // Revenue statistics
        Double totalRevenue = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .mapToDouble(Payment::getTotalAmount).sum();

        Double totalRefunds = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.REFUNDED)
                .mapToDouble(p -> p.getRefundAmount() != null ? p.getRefundAmount() : 0.0).sum();

        overview.put("totalRevenue", totalRevenue);
        overview.put("totalRefunds", totalRefunds);
        overview.put("netRevenue", totalRevenue - totalRefunds);

        // Appointment statistics
        overview.put("totalAppointments", allAppointments.size());
        overview.put("confirmedAppointments", allAppointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.CONFIRMED).count());
        overview.put("cancelledAppointments", allAppointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.CANCELLED).count());

        // Unique counts
        Set<Long> uniquePatients = allAppointments.stream()
                .map(a -> a.getPatient().getId())
                .collect(Collectors.toSet());

        Set<Long> uniqueDoctors = allAppointments.stream()
                .map(a -> a.getDoctor().getId())
                .collect(Collectors.toSet());

        overview.put("totalPatients", uniquePatients.size());
        overview.put("totalDoctors", uniqueDoctors.size());

        return overview;
    }
}