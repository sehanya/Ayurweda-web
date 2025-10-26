package com.example.ayurlink.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_charges")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCharge {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @OneToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "payment_id", nullable = false, unique = true)
        private Payment payment;

        @Column(name = "clinic_charge", nullable = false)
        private Double clinicCharge; // Fixed 500.0 LKR

        @Column(name = "total_payment_amount", nullable = false)
        private Double totalPaymentAmount; // Total payment received

        @Column(name = "doctor_earning", nullable = false)
        private Double doctorEarning; // Amount allocated to doctor

        @Column(name = "treatment_name", length = 255)
        private String treatmentName;

        @Column(name = "doctor_name", length = 255)
        private String doctorName;

        @Column(name = "patient_name", length = 255)
        private String patientName;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private ChargeStatus status = ChargeStatus.COLLECTED;

        @Column(name = "charge_date")
        private LocalDateTime chargeDate;

        @Column(name = "notes", columnDefinition = "TEXT")
        private String notes;

        @Column(name = "created_at")
        private LocalDateTime createdAt;

        @Column(name = "updated_at")
        private LocalDateTime updatedAt;

        @PrePersist
        protected void onCreate() {
            createdAt = LocalDateTime.now();
            chargeDate = LocalDateTime.now();
        }
        @PreUpdate
        protected void onUpdate() {
            updatedAt = LocalDateTime.now();
        }
    }

