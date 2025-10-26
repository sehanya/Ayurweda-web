package com.example.ayurlink.service;

import com.example.ayurlink.model.Patient;
import com.example.ayurlink.model.Role;
import com.example.ayurlink.repository.PatientRepository;
import com.example.ayurlink.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PatientService {

    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public Patient registerPatient(Patient patient) {
        // Validate NIC uniqueness
        if (patientRepository.existsByNic(patient.getNic())) {
            throw new RuntimeException("NIC already registered");
        }

        // Set role and encode password
        patient.setRole(Role.ROLE_PATIENT);
        patient.setPassword(passwordEncoder.encode(patient.getPassword()));
        patient.setIsActive(true);

        return patientRepository.save(patient);
    }

    public Optional<Patient> getPatientById(Long id) {
        return patientRepository.findById(id);
    }

    public Optional<Patient> getPatientByUsername(String username) {
        return patientRepository.findByUsername(username);
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }


    public void updatePatient(String username, Patient updated) {
        Optional<Patient> optionalPatient = patientRepository.findByUsername(username);
        if (optionalPatient.isPresent()) {
            Patient patient = optionalPatient.get();
            patient.setFullName(updated.getFullName());
            patient.setEmail(updated.getEmail());
            patient.setPhone(updated.getPhone());
            patient.setAge(updated.getAge());
            patient.setGender(updated.getGender());
            patient.setAddress(updated.getAddress());
            patient.setMedicalHistory(updated.getMedicalHistory());
            patient.setUsername(updated.getUsername());
            patientRepository.save(patient);
        }
    }

    public boolean changePassword(String username, String currentPassword, String newPassword) {
        Optional<Patient> optionalPatient = patientRepository.findByUsername(username);
        if (optionalPatient.isPresent()) {
            Patient patient = optionalPatient.get();
            // check current password
            if (passwordEncoder.matches(currentPassword, patient.getPassword())) {
                // encode and update new password
                String encodedPassword = passwordEncoder.encode(newPassword);
                patient.setPassword(encodedPassword);
                // update in DB
                patientRepository.saveAndFlush(patient);
                System.out.println("Password updated successfully for user: " + username);
                return true;
            } else {
                System.out.println("Invalid current password");
            }
        }
        return false;
    }



    public void deletePatient(Long id) {
        patientRepository.deleteById(id);
    }
}