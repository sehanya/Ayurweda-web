package com.example.ayurlink.service;

import com.example.ayurlink.model.Appointment;
import com.example.ayurlink.model.AppointmentStatus;
import com.example.ayurlink.model.Doctor;
import com.example.ayurlink.repository.AppointmentRepository;
import com.example.ayurlink.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TimeSlotService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private static final int SLOT_DURATION_MINUTES = 15;

    public List<LocalTime> getAvailableTimeSlots(Long doctorId, LocalDate date) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        String dayOfWeek = date.getDayOfWeek().toString().substring(0, 3).toUpperCase();
        LocalTime startTime = null;
        LocalTime endTime = null;

        if (doctor.getAvailability() != null && !doctor.getAvailability().isEmpty()) {
            for (String slot : doctor.getAvailability()) {
                if (slot.startsWith(dayOfWeek)) {
                    String[] parts = slot.split(" ");
                    if (parts.length == 2) {
                        String[] times = parts[1].split("-");
                        if (times.length == 2) {
                            startTime = LocalTime.parse(times[0]);
                            endTime = LocalTime.parse(times[1]);
                            break;
                        }
                    }
                }
            }
        }

        if (startTime == null || endTime == null) {
            return new ArrayList<>();
        }

        List<LocalTime> allSlots = generateTimeSlots(startTime, endTime);
        Set<LocalTime> bookedSlots = getBookedTimeSlots(doctorId, date);

        return allSlots.stream()
                .filter(slot -> !bookedSlots.contains(slot))
                .collect(Collectors.toList());
    }

    private List<LocalTime> generateTimeSlots(LocalTime start, LocalTime end) {
        List<LocalTime> slots = new ArrayList<>();
        LocalTime current = start;

        while (current.isBefore(end)) {
            slots.add(current);
            current = current.plusMinutes(SLOT_DURATION_MINUTES);
        }

        return slots;
    }

    private Set<LocalTime> getBookedTimeSlots(Long doctorId, LocalDate date) {
        List<Appointment> appointments = appointmentRepository.findByDoctorId(doctorId).stream()
                .filter(apt -> apt.getAppointmentDate().equals(date))
                .filter(apt -> apt.getStatus() != AppointmentStatus.CANCELLED)
                .collect(Collectors.toList());

        Set<LocalTime> bookedSlots = new HashSet<>();
        for (Appointment apt : appointments) {
            bookedSlots.add(apt.getAppointmentTime());
        }

        return bookedSlots;
    }

    public List<TimeSlotDTO> getAvailableTimeSlotsWithStatus(Long doctorId, LocalDate date) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        String dayOfWeek = date.getDayOfWeek().toString().substring(0, 3).toUpperCase();
        LocalTime startTime = null;
        LocalTime endTime = null;

        if (doctor.getAvailability() != null && !doctor.getAvailability().isEmpty()) {
            for (String slot : doctor.getAvailability()) {
                if (slot.startsWith(dayOfWeek)) {
                    String[] parts = slot.split(" ");
                    if (parts.length == 2) {
                        String[] times = parts[1].split("-");
                        if (times.length == 2) {
                            startTime = LocalTime.parse(times[0]);
                            endTime = LocalTime.parse(times[1]);
                            break;
                        }
                    }
                }
            }
        }

        if (startTime == null || endTime == null) {
            return new ArrayList<>();
        }

        List<LocalTime> allSlots = generateTimeSlots(startTime, endTime);
        Set<LocalTime> bookedSlots = getBookedTimeSlots(doctorId, date);

        return allSlots.stream()
                .map(time -> new TimeSlotDTO(
                        time.format(DateTimeFormatter.ofPattern("HH:mm")),
                        !bookedSlots.contains(time)
                ))
                .collect(Collectors.toList());
    }

    public boolean isTimeSlotAvailable(Long doctorId, LocalDate date, LocalTime time) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        String dayOfWeek = date.getDayOfWeek().toString().substring(0, 3).toUpperCase();
        LocalTime startTime = null;
        LocalTime endTime = null;

        if (doctor.getAvailability() != null && !doctor.getAvailability().isEmpty()) {
            for (String slot : doctor.getAvailability()) {
                if (slot.startsWith(dayOfWeek)) {
                    String[] parts = slot.split(" ");
                    if (parts.length == 2) {
                        String[] times = parts[1].split("-");
                        if (times.length == 2) {
                            startTime = LocalTime.parse(times[0]);
                            endTime = LocalTime.parse(times[1]);
                            break;
                        }
                    }
                }
            }
        }

        if (startTime == null || endTime == null ||
                time.isBefore(startTime) || !time.isBefore(endTime)) {
            return false;
        }

        Set<LocalTime> bookedSlots = getBookedTimeSlots(doctorId, date);
        return !bookedSlots.contains(time);
    }

    public static class TimeSlotDTO {
        public String time;
        public boolean available;

        public TimeSlotDTO(String time, boolean available) {
            this.time = time;
            this.available = available;
        }

        public String getTime() {
            return time;
        }

        public boolean isAvailable() {
            return available;
        }
    }
}