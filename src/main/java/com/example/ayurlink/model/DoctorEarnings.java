package com.example.ayurlink.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "doctor_earnings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorEarning {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false, unique = true)
    private Payment payment;

    @Column(name = "gross_amount", nullable = false)
    private Double grossAmount; // Total payment amount

    @Column(name = "admin_charge", nullable = false)
    private Double adminCharge; // Clinic charges deducted

    @Column(name = "net_earning", nullable = false)
    private Double netEarning; // Amount doctor receives (doctorFee + treatmentFee)

    @Column(name = "doctor_fee", nullable = false)
    private Double doctorFee; // Consultation fee portion

    @Column(name = "treatment_fee", nullable = false)
    private Double treatmentFee; // Treatment fee portion

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EarningStatus status = EarningStatus.PENDING;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "settlement_date")
    private LocalDateTime settlementDate; // When doctor receives the payment

    @Column(name = "settlement_reference", length = 100)
    private String settlementReference; // Bank transfer reference, etc.

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        paymentDate = LocalDateTime.now();
        calculateNetEarning();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Calculate net earning: doctorFee + treatmentFee (excludes admin charge)
     */
    public void calculateNetEarning() {
        this.netEarning = this.doctorFee + this.treatmentFee;
    }

    /**
     * Mark earning as settled/paid to doctor
     */
    public void markAsSettled(String reference) {
        this.status = EarningStatus.SETTLED;
        this.settlementDate = LocalDateTime.now();
        this.settlementReference = reference;
    }
}

