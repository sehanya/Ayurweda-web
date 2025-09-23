package com.example.AyurvedaWeb.controller;
import org.springframework.web.bind.annotation.*;
import java.util.List;
public class AppointmentController {


    @RestController
    @RequestMapping("/doctors")
    public class DoctorController {

        private final DoctorRepository doctorRepo;
        private final DoctorAvailabilityRepository availabilityRepo;

        public DoctorController(DoctorRepository doctorRepo, DoctorAvailabilityRepository availabilityRepo) {
            this.doctorRepo = doctorRepo;
            this.availabilityRepo = availabilityRepo;
        }

        // CREATE doctor
        @PostMapping
        public Doctor addDoctor(@RequestBody Doctor doctor) {
            return doctorRepo.save(doctor);
        }

        // READ all doctors
        @GetMapping
        public List<Doctor> getDoctors() {
            return doctorRepo.findAll();
        }

        // READ single doctor by ID
        @GetMapping("/{id}")
        public Doctor getDoctor(@PathVariable Long id) {
            return doctorRepo.findById(id).orElseThrow(() -> new RuntimeException("Doctor not found"));
        }

        // UPDATE doctor
        @PutMapping("/{id}")
        public Doctor updateDoctor(@PathVariable Long id, @RequestBody Doctor updatedDoctor) {
            return doctorRepo.findById(id).map(doctor -> {
                doctor.setName(updatedDoctor.getName());
                doctor.setSpecialization(updatedDoctor.getSpecialization());
                doctor.setFee(updatedDoctor.getFee());
                doctor.setBio(updatedDoctor.getBio());
                doctor.setContact(updatedDoctor.getContact());
                return doctorRepo.save(doctor);
            }).orElseThrow(() -> new RuntimeException("Doctor not found"));
        }

        // DELETE doctor
        @DeleteMapping("/{id}")
        public void deleteDoctor(@PathVariable Long id) {
            doctorRepo.deleteById(id);
        }

        // ADD availability for doctor
        @PostMapping("/{id}/availability")
        public DoctorAvailability addAvailability(@PathVariable Long id, @RequestBody DoctorAvailability slot) {
            Doctor doctor = doctorRepo.findById(id).orElseThrow(() -> new RuntimeException("Doctor not found"));
            slot.setDoctor(doctor);
            return availabilityRepo.save(slot);
        }

        // VIEW availability for doctor
        @GetMapping("/{id}/availability")
        public List<DoctorAvailability> getAvailability(@PathVariable Long id) {
            Doctor doctor = doctorRepo.findById(id).orElseThrow(() -> new RuntimeException("Doctor not found"));
            return doctor.getAvailability();
        }

        // DELETE availability slot
        @DeleteMapping("/availability/{slotId}")
        public void deleteAvailability(@PathVariable Long slotId) {
            availabilityRepo.deleteById(slotId);
        }
    }

}
