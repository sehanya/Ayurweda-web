package com.example.ayurlink.service;

import com.example.ayurlink.model.Doctor;
import com.example.ayurlink.model.Role;
import com.example.ayurlink.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;

    public Doctor createDoctor(Doctor doctor, String plainPassword) {
        // Set role and encode password
        doctor.setRole(Role.ROLE_DOCTOR);
        doctor.setPassword(passwordEncoder.encode(plainPassword));
        doctor.setIsActive(true);

        return doctorRepository.save(doctor);
    }

    public Optional<Doctor> getDoctorById(Long id) {
        return doctorRepository.findById(id);
    }

    public Optional<Doctor> getDoctorByUsername(String username) {
        return doctorRepository.findByUsername(username);
    }

    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    public List<Doctor> getDoctorsBySpecialization(String specialization) {
        return doctorRepository.findBySpecialization(specialization);
    }

    public Doctor updateDoctor(Long id, Doctor updatedDoctor) {
        Doctor existing = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        existing.setFullName(updatedDoctor.getFullName());
        existing.setEmail(updatedDoctor.getEmail());
        existing.setPhone(updatedDoctor.getPhone());
        existing.setSpecialization(updatedDoctor.getSpecialization());
        existing.setLicenseNumber(updatedDoctor.getLicenseNumber());
        existing.setConsultationFee(updatedDoctor.getConsultationFee());
        existing.setBio(updatedDoctor.getBio());
        existing.setAvailability(updatedDoctor.getAvailability());

        return doctorRepository.save(existing);
    }

    public Doctor updateAvailability(Long doctorId, List<String> availability) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        doctor.setAvailability(availability);
        return doctorRepository.save(doctor);
    }

    public void deleteDoctor(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        doctor.setIsActive(false);
        doctorRepository.save(doctor);
    }
}