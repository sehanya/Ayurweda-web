package com.example.ayurlink.repository;

import com.example.ayurlink.model.AdminCharge;
import com.example.ayurlink.model.ChargeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdminChargeRepository extends JpaRepository<AdminCharge, Long> {

    Optional<AdminCharge> findByPaymentId(Long paymentId);

    List<AdminCharge> findByStatus(ChargeStatus status);

    List<AdminCharge> findByChargeDateBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT SUM(a.clinicCharge) FROM AdminCharge a WHERE a.status = :status")
    Double getTotalChargesByStatus(@Param("status") ChargeStatus status);

    @Query("SELECT SUM(a.clinicCharge) FROM AdminCharge a WHERE a.chargeDate BETWEEN :start AND :end")
    Double getTotalChargesByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}