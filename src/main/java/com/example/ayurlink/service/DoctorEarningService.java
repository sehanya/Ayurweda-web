package com.example.ayurlink.service;

import com.example.ayurlink.model.*;
import com.example.ayurlink.repository.AdminChargeRepository;
import com.example.ayurlink.repository.DoctorEarningRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DoctorEarningService {

    private final DoctorEarningRepository doctorEarningRepository;
    private final AdminChargeRepository adminChargeRepository;

    /**
     * Create earning records when payment is verified/approved by admin
     * Called after admin approves the payment
     */
    public void createEarningRecords(Payment payment) {
        log.info("Creating earning records for payment ID: {}", payment.getId());

        // Create Doctor Earning record
        DoctorEarning doctorEarning = new DoctorEarning();
        doctorEarning.setDoctor(payment.getAppointment().getDoctor());
        doctorEarning.setPayment(payment);
        doctorEarning.setGrossAmount(payment.getTotalAmount());
        doctorEarning.setAdminCharge(payment.getClinicCharges());
        doctorEarning.setDoctorFee(payment.getDoctorFee());
        doctorEarning.setTreatmentFee(payment.getTreatmentFee());
        doctorEarning.setStatus(EarningStatus.PENDING);
        doctorEarning.calculateNetEarning();

        DoctorEarning savedEarning = doctorEarningRepository.save(doctorEarning);
        log.info("Doctor earning created - Net: LKR {}", savedEarning.getNetEarning());

        // Create Admin Charge record
        AdminCharge adminCharge = new AdminCharge();
        adminCharge.setPayment(payment);
        adminCharge.setClinicCharge(payment.getClinicCharges());
        adminCharge.setTotalPaymentAmount(payment.getTotalAmount());
        adminCharge.setDoctorEarning(savedEarning.getNetEarning());
        adminCharge.setTreatmentName(payment.getAppointment().getTreatment().getName());
        adminCharge.setDoctorName(payment.getAppointment().getDoctor().getFullName());
        adminCharge.setPatientName(payment.getAppointment().getPatient().getFullName());
        adminCharge.setStatus(ChargeStatus.COLLECTED);

        AdminCharge savedCharge = adminChargeRepository.save(adminCharge);
        log.info("Admin charge recorded - Amount: LKR {}", savedCharge.getClinicCharge());
    }

    /**
     * Get all earnings for a specific doctor
     */
    public List<DoctorEarning> getDoctorEarnings(Long doctorId) {
        return doctorEarningRepository.findByDoctorId(doctorId);
    }

    /**
     * Get pending earnings for a doctor
     */
    public List<DoctorEarning> getPendingEarnings(Long doctorId) {
        return doctorEarningRepository.findByDoctorIdAndStatus(doctorId, EarningStatus.PENDING);
    }

    /**
     * Get settled earnings for a doctor
     */
    public List<DoctorEarning> getSettledEarnings(Long doctorId) {
        return doctorEarningRepository.findByDoctorIdAndStatus(doctorId, EarningStatus.SETTLED);
    }

    /**
     * Calculate total earnings for a doctor (all time)
     */
    public Double getTotalDoctorEarnings(Long doctorId) {
        List<DoctorEarning> earnings = doctorEarningRepository.findByDoctorId(doctorId);
        return earnings.stream()
                .filter(e -> e.getStatus() == EarningStatus.PENDING || e.getStatus() == EarningStatus.SETTLED)
                .mapToDouble(DoctorEarning::getNetEarning)
                .sum();
    }

    /**
     * Calculate pending earnings for a doctor
     */
    public Double getPendingEarningsAmount(Long doctorId) {
        return doctorEarningRepository.getTotalEarningsByDoctorAndStatus(doctorId, EarningStatus.PENDING);
    }

    /**
     * Calculate settled earnings for a doctor
     */
    public Double getSettledEarningsAmount(Long doctorId) {
        return doctorEarningRepository.getTotalEarningsByDoctorAndStatus(doctorId, EarningStatus.SETTLED);
    }

    /**
     * Get earnings for a specific date range
     */
    public List<DoctorEarning> getDoctorEarningsByDateRange(Long doctorId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        return doctorEarningRepository.findByDoctorIdAndPaymentDateBetween(doctorId, start, end);
    }

    /**
     * Calculate earnings for today
     */
    public Double getTodayEarnings(Long doctorId) {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.atTime(LocalTime.MAX);

        Double amount = doctorEarningRepository.getTotalEarningsByDoctorAndDateRange(doctorId, start, end);
        return amount != null ? amount : 0.0;
    }

    /**
     * Calculate earnings for this week
     */
    public Double getWeekEarnings(Long doctorId) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDateTime start = startOfWeek.atStartOfDay();
        LocalDateTime end = LocalDateTime.now();

        Double amount = doctorEarningRepository.getTotalEarningsByDoctorAndDateRange(doctorId, start, end);
        return amount != null ? amount : 0.0;
    }

    /**
     * Calculate earnings for this month
     */
    public Double getMonthEarnings(Long doctorId) {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDateTime start = startOfMonth.atStartOfDay();
        LocalDateTime end = LocalDateTime.now();

        Double amount = doctorEarningRepository.getTotalEarningsByDoctorAndDateRange(doctorId, start, end);
        return amount != null ? amount : 0.0;
    }

    /**
     * Get detailed earning summary for doctor
     */
    public Map<String, Object> getDoctorEarningSummary(Long doctorId) {
        Map<String, Object> summary = new HashMap<>();

        Double totalEarnings = getTotalDoctorEarnings(doctorId);
        Double pendingAmount = getPendingEarningsAmount(doctorId);
        Double settledAmount = getSettledEarningsAmount(doctorId);
        Double todayEarnings = getTodayEarnings(doctorId);
        Double weekEarnings = getWeekEarnings(doctorId);
        Double monthEarnings = getMonthEarnings(doctorId);

        summary.put("totalEarnings", totalEarnings != null ? totalEarnings : 0.0);
        summary.put("pendingAmount", pendingAmount != null ? pendingAmount : 0.0);
        summary.put("settledAmount", settledAmount != null ? settledAmount : 0.0);
        summary.put("todayEarnings", todayEarnings);
        summary.put("weekEarnings", weekEarnings);
        summary.put("monthEarnings", monthEarnings);

        summary.put("pendingEarnings", getPendingEarnings(doctorId));
        summary.put("settledEarnings", getSettledEarnings(doctorId));

        return summary;
    }

    /**
     * Mark earning as settled (when doctor receives payment)
     */
    public DoctorEarning settleEarning(Long earningId, String settlementReference, String notes) {
        DoctorEarning earning = doctorEarningRepository.findById(earningId)
                .orElseThrow(() -> new RuntimeException("Earning not found"));

        earning.markAsSettled(settlementReference);
        if (notes != null) {
            earning.setNotes(notes);
        }

        return doctorEarningRepository.save(earning);
    }

    /**
     * Get earning by payment ID
     */
    public Optional<DoctorEarning> getEarningByPayment(Long paymentId) {
        return doctorEarningRepository.findByPaymentId(paymentId);
    }

    /**
     * Handle refund - cancel earning records
     */
    public void handleRefund(Payment payment) {
        Optional<DoctorEarning> earningOpt = doctorEarningRepository.findByPaymentId(payment.getId());
        if (earningOpt.isPresent()) {
            DoctorEarning earning = earningOpt.get();
            earning.setStatus(EarningStatus.CANCELLED);
            doctorEarningRepository.save(earning);
            log.info("Doctor earning cancelled due to refund");
        }

        Optional<AdminCharge> chargeOpt = adminChargeRepository.findByPaymentId(payment.getId());
        if (chargeOpt.isPresent()) {
            AdminCharge charge = chargeOpt.get();
            charge.setStatus(ChargeStatus.REFUNDED);
            adminChargeRepository.save(charge);
            log.info("Admin charge marked as refunded");
        }
    }

    // ==================== ADMIN CHARGE QUERIES ====================

    /**
     * Get total admin charges collected
     */
    public Double getTotalAdminCharges() {
        return adminChargeRepository.getTotalChargesByStatus(ChargeStatus.COLLECTED);
    }

    /**
     * Get admin charges for date range
     */
    public Double getAdminChargesByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        return adminChargeRepository.getTotalChargesByDateRange(start, end);
    }

    /**
     * Get all admin charge records
     */
    public List<AdminCharge> getAllAdminCharges() {
        return adminChargeRepository.findAll();
    }
}