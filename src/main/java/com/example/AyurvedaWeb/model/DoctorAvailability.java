package com.example.AyurvedaWeb.model;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import com.example.AyurvedaWeb.model.Doctor;

@Entity
public class DoctorAvailability {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private int slotLength; // minutes

    // Getters and setters


    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }
}
