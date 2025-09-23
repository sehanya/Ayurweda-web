package com.example.AyurvedaWeb.repository;
import org.springframework.data.jpa.repository.JpaRepository;
public class Doctor {

    public interface DoctorRepository extends JpaRepository<Doctor, Long> {}

    public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, Long> {}

}
