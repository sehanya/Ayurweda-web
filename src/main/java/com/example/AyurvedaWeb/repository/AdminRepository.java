package com.example.AyurvedaWeb.repository;

import com.example.AyurvedaWeb.model.Admin;
import com.example.AyurvedaWeb.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByUsername(String username);
    Optional<Admin> findByEmail(String email);
    List<Admin> findByRole(Role role);
    List<Admin> findByIsActiveTrue();
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}