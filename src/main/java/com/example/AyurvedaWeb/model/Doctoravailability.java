package com.example.AyurvedaWeb.model;
import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
public class Doctoravailability {

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
    }

}
