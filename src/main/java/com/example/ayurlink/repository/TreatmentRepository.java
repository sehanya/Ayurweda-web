package com.example.ayurlink.repository;

import com.example.ayurlink.model.Treatment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TreatmentRepository extends JpaRepository<Treatment, Long> {
    List<Treatment> findByIsActiveTrue();
    List<Treatment> findByNameContainingIgnoreCase(String name);
}