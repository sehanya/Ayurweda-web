package com.example.ayurlink.model;

public enum PaymentStatus {
    PENDING,              // Payment initiated, awaiting completion
    PENDING_VERIFICATION, // Receipt uploaded, awaiting admin verification
    SUCCESS,              // Payment completed and verified
    COMPLETED,            // Alternative for SUCCESS
    FAILED,               // Payment failed
    REJECTED,             // Receipt rejected by admin
    REFUNDED,             // Payment refunded
    CANCELLED
}