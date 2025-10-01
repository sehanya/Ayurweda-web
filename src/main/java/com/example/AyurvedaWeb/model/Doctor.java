package com.example.AyurvedaWeb.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name="Doctor")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String specialization;
    private double fee;
    private String bio;
    private String contact;

    // One doctor can have many availability slots
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL)
    private List<DoctorAvailability> availability;

    // Constructors
    public Doctor() {}

    public Doctor(String name, String specialization, double fee, String bio, String contact) {
        this.name = name;
        this.specialization = specialization;
        this.fee = fee;
        this.bio = bio;
        this.contact = contact;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public double getFee() { return fee; }
    public void setFee(double fee) { this.fee = fee; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public List<DoctorAvailability> getAvailability() { return availability; }
    public void setAvailability(List<DoctorAvailability> availability) { this.availability = availability; }
}
