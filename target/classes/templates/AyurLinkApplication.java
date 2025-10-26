package com.example.ayurlink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class AyurLinkApplication {
    public static void main(String[] args) {
        SpringApplication.run(AyurLinkApplication.class, args);

        try {
            Path uploadPath = Paths.get("uploads/receipts");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("✓ Created upload directory: " + uploadPath.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("✗ Could not create upload directory: " + e.getMessage());
        }
    }
}