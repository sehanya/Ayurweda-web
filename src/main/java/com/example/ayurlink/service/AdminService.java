package com.example.ayurlink.service;

import com.example.ayurlink.model.Admin;
import com.example.ayurlink.model.Role;
import com.example.ayurlink.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public Admin createAdmin(Admin admin, String plainPassword, Boolean isSuperAdmin) {
        // Set role based on super admin status
        if (isSuperAdmin) {
            admin.setRole(Role.ROLE_SUPER_ADMIN);
            admin.setIsSuperAdmin(true);
        } else {
            admin.setRole(Role.ROLE_ADMIN);
            admin.setIsSuperAdmin(false);
        }

        admin.setPassword(passwordEncoder.encode(plainPassword));
        admin.setIsActive(true);

        return adminRepository.save(admin);
    }

    public Optional<Admin> getAdminById(Long id) {
        return adminRepository.findById(id);
    }

    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    public List<Admin> getSuperAdmins() {
        return adminRepository.findByIsSuperAdmin(true);
    }

    public List<Admin> getRegularAdmins() {
        return adminRepository.findByIsSuperAdmin(false);
    }

    public Admin updateAdmin(Long id, Admin updatedAdmin) {
        Admin existing = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        existing.setFullName(updatedAdmin.getFullName());
        existing.setEmail(updatedAdmin.getEmail());
        existing.setPhone(updatedAdmin.getPhone());

        return adminRepository.save(existing);
    }

    public void deactivateAdmin(Long id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        admin.setIsActive(false);
        adminRepository.save(admin);
    }

}