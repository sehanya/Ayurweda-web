package com.example.ayurlink.repository;

import com.example.ayurlink.model.DoctorEarning;
import com.example.ayurlink.model.EarningStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorEarningRepository extends JpaRepository<DoctorEarning, Long> {

    List<DoctorEarning> findByDoctorId(Long doctorId);

    List<DoctorEarning> findByDoctorIdAndStatus(Long doctorId, EarningStatus status);

    Optional<DoctorEarning> findByPaymentId(Long paymentId);

    List<DoctorEarning> findByPaymentDateBetween(LocalDateTime start, LocalDateTime end);

    List<DoctorEarning> findByDoctorIdAndPaymentDateBetween(Long doctorId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT SUM(e.netEarning) FROM DoctorEarning e WHERE e.doctor.id = :doctorId AND e.status = :status")
    Double getTotalEarningsByDoctorAndStatus(@Param("doctorId") Long doctorId, @Param("status") EarningStatus status);

    @Query("SELECT SUM(e.netEarning) FROM DoctorEarning e WHERE e.doctor.id = :doctorId " +
            "AND e.paymentDate BETWEEN :start AND :end")
    Double getTotalEarningsByDoctorAndDateRange(@Param("doctorId") Long doctorId,
                                                @Param("start") LocalDateTime start,
                                                @Param("end") LocalDateTime end);
}
