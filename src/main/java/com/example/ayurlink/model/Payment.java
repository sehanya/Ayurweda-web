package com.example.ayurlink.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;



    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private Appointment appointment;

    // Breakdown of charges
    @Column(name = "doctor_fee", nullable = false)
    private Double doctorFee;

    @Column(name = "treatment_fee", nullable = false)
    private Double treatmentFee;

    @Column(name = "clinic_charges", nullable = false)
    private Double clinicCharges = 500.0;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    // Payment details
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "transaction_id", unique = true)
    private String transactionId;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    // Receipt upload (for RECEIPT_UPLOAD method)
    @Column(name = "receipt_file_path" ,length = 500)
    private String receiptFilePath;

    @Column(name = "receipt_file_name", length = 255)
    private String receiptFileName;

    @Column(name = "receipt_upload_date")
    private LocalDateTime receiptUploadDate;

    @Column(name = "receipt_verified")
    private Boolean receiptVerified = false;

    @Column(name = "verified_by"  ,length = 100)
    private String verifiedBy; // Admin who verified receipt

    @Column(name = "verification_date")
    private LocalDateTime verificationDate;

    // Refund information
    @Column(name = "refund_amount")
    private Double refundAmount;

    @Column(name = "refund_date")
    private LocalDateTime refundDate;

    @Column(name = "refund_reason" , columnDefinition = "TEXT")
    private String refundReason;

    // Additional details
    @Column(name = "payment_notes", columnDefinition = "TEXT")
    private String paymentNotes;

    @Column(name = "receipt_number", unique = true ,length = 100)
    private String receiptNumber;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        paymentDate = LocalDateTime.now();
        generateTransactionId();
        generateReceiptNumber();
        calculateTotal();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    private void generateTransactionId() {
        this.transactionId = "TXN" + System.currentTimeMillis() +
                String.format("%04d", (int)(Math.random() * 10000));
    }

    private void generateReceiptNumber() {
        this.receiptNumber = "RCP" + System.currentTimeMillis();
    }

    public void calculateTotal() {
        this.totalAmount = this.doctorFee + this.treatmentFee + this.clinicCharges;
    }
}