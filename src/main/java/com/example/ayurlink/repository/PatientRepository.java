package com.example.ayurlink.repository;

import com.example.ayurlink.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;


@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByNic(String nic);
    Optional<Patient> findByUsername(String username);
    boolean existsByNic(String nic);
}