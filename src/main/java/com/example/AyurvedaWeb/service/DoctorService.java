package com.example.AyurvedaWeb.service;

import com.example.AyurvedaWeb.model.Doctor;
import com.example.AyurvedaWeb.repository.DoctorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DoctorService {
    private final DoctorRepository repository;

    public DoctorService(DoctorRepository repository) {
        this.repository = repository;
    }

    public List<Doctor> getAllDoctors() {
        return repository.findAll();
    }



    public Doctor addDoctor(Doctor doctor) {
        return repository.save(doctor);
    }

    public Optional<Doctor> getDoctorById(Long id) {
        return repository.findById(id);
    }

    public Doctor updateDoctor(Long id, Doctor updatedDoctor) {
        return repository.findById(id).map(doctor -> {
            doctor.setFullName(updatedDoctor.getFullName());
            doctor.setSpecialization(updatedDoctor.getSpecialization());
            doctor.setEmail(updatedDoctor.getEmail());
            doctor.setPhone(updatedDoctor.getPhone());
            doctor.setPhoto(updatedDoctor.getPhoto());
            return repository.save(doctor);
        }).orElseThrow(() -> new RuntimeException("Doctor not found"));
    }

    public void deleteDoctor(Long id) {
        repository.deleteById(id);
    }
}
