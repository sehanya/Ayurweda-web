package com.example.AyurvedaWeb.service;

import com.example.AyurvedaWeb.DTO.AdminDTO;
import com.example.AyurvedaWeb.model.Admin;
import com.example.AyurvedaWeb.model.Role;
import com.example.AyurvedaWeb.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    public List<Admin> getActiveAdmins() {
        return adminRepository.findByIsActiveTrue();
    }

    public Optional<Admin> getAdminById(Long id) {
        return adminRepository.findById(id);
    }

    public Optional<Admin> getAdminByUsername(String username) {
        return adminRepository.findByUsername(username);
    }

    public Admin createAdmin(AdminDTO adminDTO) {
        if (adminRepository.existsByUsername(adminDTO.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (adminRepository.existsByEmail(adminDTO.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        Admin admin = new Admin();
        admin.setUsername(adminDTO.getUsername());
        admin.setPassword(passwordEncoder.encode(adminDTO.getPassword()));
        admin.setEmail(adminDTO.getEmail());
        admin.setFullName(adminDTO.getFullName());
        admin.setRole(adminDTO.getRole());
        admin.setIsActive(true);

        return adminRepository.save(admin);
    }

    public Admin updateAdmin(Long id, AdminDTO adminDTO) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        admin.setEmail(adminDTO.getEmail());
        admin.setFullName(adminDTO.getFullName());
        admin.setRole(adminDTO.getRole());

        if (adminDTO.getPassword() != null && !adminDTO.getPassword().isEmpty()) {
            admin.setPassword(passwordEncoder.encode(adminDTO.getPassword()));
        }

        return adminRepository.save(admin);
    }

    public void deactivateAdmin(Long id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        admin.setIsActive(false);
        adminRepository.save(admin);
    }

    public void activateAdmin(Long id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        admin.setIsActive(true);
        adminRepository.save(admin);
    }

    public void deleteAdmin(Long id) {
        adminRepository.deleteById(id);
    }

    public boolean hasRole(String username, Role role) {
        Optional<Admin> admin = adminRepository.findByUsername(username);
        return admin.isPresent() && admin.get().getRole() == role;
    }
}