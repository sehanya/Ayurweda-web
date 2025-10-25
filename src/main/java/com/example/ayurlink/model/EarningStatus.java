package com.example.ayurlink.model;

public enum EarningStatus {
    PENDING,        // Payment verified but not yet settled to doctor
    SETTLED,        // Amount paid to doctor
    CANCELLED       // If payment was refunded
}

