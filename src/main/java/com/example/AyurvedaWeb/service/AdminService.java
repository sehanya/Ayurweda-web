package com.example.AyurvedaWeb.service;

import com.example.AyurvedaWeb.model.Admin;
import com.example.AyurvedaWeb.repository.AdminRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AdminService {
    private final AdminRepository adminRepository;

    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }


}
