package com.example.ayurlink.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "file.upload")
@Data
public class FileStorageProperties {
    private String uploadDir = "uploads/receipts";
}