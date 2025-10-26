package com.example.ayurlink.repository;

import com.example.ayurlink.model.Appointment;
import com.example.ayurlink.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientId(Long patientId);
    List<Appointment> findByDoctorId(Long doctorId);
    List<Appointment> findByAppointmentDate(LocalDate date);
    Optional<Appointment> findByTicketNumber(String ticketNumber);
    List<Appointment> findByStatus(AppointmentStatus status);
    List<Appointment> findByDoctorIdAndStatus(Long doctorId, AppointmentStatus status);

    List<Appointment> findByDoctorIdAndAppointmentDateAndAppointmentTime(
            Long doctorId, LocalDate date, LocalTime time);
}