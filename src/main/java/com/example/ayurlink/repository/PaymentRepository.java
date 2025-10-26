package com.example.ayurlink.repository;

import com.example.ayurlink.model.Payment;
import com.example.ayurlink.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByAppointmentId(Long appointmentId);
    List<Payment> findByAppointment_Patient_Id(Long patientId);
    List<Payment> findByPaymentDateBetween(LocalDateTime start, LocalDateTime end);
    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByStatusOrderByPaymentDateDesc(PaymentStatus status);

    List<Payment> findByPaymentDateBetweenOrderByPaymentDateDesc(LocalDateTime start, LocalDateTime end);

    @Query("SELECT SUM(p.totalAmount) FROM Payment p WHERE p.status IN ('SUCCESS', 'COMPLETED') " +
            "AND p.paymentDate BETWEEN :start AND :end")
    Double getTotalRevenueBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}