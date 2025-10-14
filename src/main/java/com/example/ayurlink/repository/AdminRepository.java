package com.example.ayurlink.repository;

import com.example.ayurlink.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    List<Admin> findByIsSuperAdmin(Boolean isSuperAdmin);
}