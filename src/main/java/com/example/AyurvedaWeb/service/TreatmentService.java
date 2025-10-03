
package com.example.AyurvedaWeb.service;

import com.example.AyurvedaWeb.DTO.TreatmentDTO;
import com.example.AyurvedaWeb.model.Treatment;
import com.example.AyurvedaWeb.repository.TreatmentRepository;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class TreatmentService {

    private final TreatmentRepository treatmentRepository;

    public TreatmentService(TreatmentRepository treatmentRepository) {
        this.treatmentRepository = treatmentRepository;
    }

    // Get all treatments
    public List<Treatment> getAllTreatments() {
        return treatmentRepository.findAll();
    }

    // Create new treatment
    public Treatment createTreatment(TreatmentDTO dto) {
        Treatment t = new Treatment();
        t.setName(dto.getName());
        t.setDescription(dto.getDescription());
        t.setPrice(dto.getPrice());
        t.setDurationMinutes(dto.getDurationMinutes());
        t.setIsActive(true);
        return treatmentRepository.save(t);
    }

    // Update treatment
    public Treatment updateTreatment(Long id, TreatmentDTO dto) {
        Treatment t = treatmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Treatment not found"));
        t.setName(dto.getName());
        t.setDescription(dto.getDescription());
        t.setPrice(dto.getPrice());
        t.setDurationMinutes(dto.getDurationMinutes());
        return treatmentRepository.save(t);
    }

    // Deactivate a treatment
    public void deactivateTreatment(Long id) {
        Treatment t = treatmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Treatment not found"));
        t.setIsActive(false);
        treatmentRepository.save(t);
    }

    // Get single treatment
    public Treatment getTreatment(Long id) {
        return treatmentRepository.findById(id).orElse(null);
    }

    // Save treatment directly (used by controller for raw entity saving)
    public Treatment saveTreatment(Treatment treatment) {
        return treatmentRepository.save(treatment);
    }

    // Delete treatment (hard delete, not just deactivate)
    public void deleteTreatment(Long id) {
        if (treatmentRepository.existsById(id)) {
            treatmentRepository.deleteById(id);
        }
    }
}
