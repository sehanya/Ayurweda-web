package com.example.AyurvedaWeb.service;

import com.example.AyurvedaWeb.DTO.TreatmentDTO;
import com.example.AyurvedaWeb.model.Treatment;
import com.example.AyurvedaWeb.repository.TreatmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TreatmentService {

    @Autowired
    private TreatmentRepository treatmentRepository;

    public List<Treatment> getAllTreatments() {
        return treatmentRepository.findAll();
    }

    public List<Treatment> getActiveTreatments() {
        return treatmentRepository.findByIsActiveTrue();
    }

    public Optional<Treatment> getTreatmentById(Long id) {
        return treatmentRepository.findById(id);
    }

    public Treatment createTreatment(TreatmentDTO treatmentDTO) {
        Treatment treatment = new Treatment();
        treatment.setName(treatmentDTO.getName());
        treatment.setDescription(treatmentDTO.getDescription());
        treatment.setPrice(treatmentDTO.getPrice());
        treatment.setDurationMinutes(treatmentDTO.getDurationMinutes());
        treatment.setIsActive(true);

        return treatmentRepository.save(treatment);
    }

    public Treatment updateTreatment(Long id, TreatmentDTO treatmentDTO) {
        Treatment treatment = treatmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Treatment not found"));

        treatment.setName(treatmentDTO.getName());
        treatment.setDescription(treatmentDTO.getDescription());
        treatment.setPrice(treatmentDTO.getPrice());
        treatment.setDurationMinutes(treatmentDTO.getDurationMinutes());

        return treatmentRepository.save(treatment);
    }

    public void deactivateTreatment(Long id) {
        Treatment treatment = treatmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Treatment not found"));
        treatment.setIsActive(false);
        treatmentRepository.save(treatment);
    }

    public void activateTreatment(Long id) {
        Treatment treatment = treatmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Treatment not found"));
        treatment.setIsActive(true);
        treatmentRepository.save(treatment);
    }

    public void deleteTreatment(Long id) {
        treatmentRepository.deleteById(id);
    }

    public List<Treatment> searchTreatments(String keyword) {
        return treatmentRepository.findByNameContainingIgnoreCase(keyword);
    }
}