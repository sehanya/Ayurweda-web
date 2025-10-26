package com.example.ayurlink.service;

import com.example.ayurlink.model.Treatment;
import com.example.ayurlink.repository.TreatmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class TreatmentService {

    private final TreatmentRepository treatmentRepository;
    private final FileStorageService fileStorageService;

    public Treatment createTreatment(Treatment treatment,MultipartFile imageFile) {
        treatment.setIsActive(true);

        // Handle image upload
        if (imageFile != null && !imageFile.isEmpty()) {
            System.out.println("=== IMAGE UPLOAD DEBUG ===");
            System.out.println("Original filename: " + imageFile.getOriginalFilename());
            System.out.println("File size: " + imageFile.getSize());
            System.out.println("Content type: " + imageFile.getContentType());

            String fileName = fileStorageService.storeFile(imageFile, "treatment");
            treatment.setImage(fileName);

            System.out.println("Stored filename: " + fileName);
            System.out.println("=== END DEBUG ===");
        } else {
            System.out.println("No image file provided or file is empty");
        }

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

    public Treatment updateTreatment(Long id, Treatment updatedTreatment,MultipartFile imageFile) {
        Treatment existing = treatmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Treatment not found"));

        existing.setName(updatedTreatment.getName());
        existing.setDescription(updatedTreatment.getDescription());
        existing.setCost(updatedTreatment.getCost());
        existing.setDuration(updatedTreatment.getDuration());
        existing.setSuitableFor(updatedTreatment.getSuitableFor());

        // Handle image upload for update
        if (imageFile != null && !imageFile.isEmpty()) {
            // Delete old image if exists
            if (existing.getImage() != null) {
                fileStorageService.deleteFile(existing.getImage());
            }
            String fileName = fileStorageService.storeFile(imageFile, "treatment");
            existing.setImage(fileName);
        }
        return treatmentRepository.save(existing);
    }

    public void deleteTreatment(Long id) {
        Treatment treatment = treatmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Treatment not found"));

        // Delete image file
        if (treatment.getImage() != null && !treatment.getImage().trim().isEmpty()) {
            fileStorageService.deleteFile(treatment.getImage());
        }

        // Delete from database
        treatmentRepository.deleteById(id);
    }

    public void deactivateTreatment(Long id) {
        Treatment treatment = treatmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Treatment not found"));
        treatment.setIsActive(false);
        treatmentRepository.save(treatment);
    }
}