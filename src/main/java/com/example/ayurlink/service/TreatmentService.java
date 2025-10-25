package com.example.ayurlink.service;

import com.example.ayurlink.model.Treatment;
import com.example.ayurlink.repository.TreatmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class TreatmentService {

    private final TreatmentRepository treatmentRepository;

    public Treatment createTreatment(Treatment treatment) {
        treatment.setIsActive(true);
        return treatmentRepository.save(treatment);
    }

    public Optional<Treatment> getTreatmentById(Long id) {
        return treatmentRepository.findById(id);
    }

    public List<Treatment> getAllTreatments() {
        return treatmentRepository.findAll();
    }

    public List<Treatment> getActiveTreatments() {
        return treatmentRepository.findByIsActiveTrue();
    }

    public Treatment findById(Long id) {
        return treatmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Treatment not found with id: " + id));
    }

    public List<Treatment> searchTreatments(String keyword) {
        return treatmentRepository.findByNameContainingIgnoreCase(keyword);
    }

    public Treatment updateTreatment(Long id, Treatment updatedTreatment) {
        Treatment existing = treatmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Treatment not found"));

        existing.setName(updatedTreatment.getName());
        existing.setDescription(updatedTreatment.getDescription());
        existing.setCost(updatedTreatment.getCost());
        existing.setDuration(updatedTreatment.getDuration());
        existing.setSuitableFor(updatedTreatment.getSuitableFor());
        existing.setImage(updatedTreatment.getImage());
        return treatmentRepository.save(existing);
    }

    public void deleteTreatment(Long id) {
        Treatment treatment = treatmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Treatment not found"));
        treatment.setIsActive(false);
        treatmentRepository.save(treatment);
    }
}