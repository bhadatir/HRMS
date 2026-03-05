package com.example.HRMS.Backend.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.HRMS.Backend.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryServiceImpl implements CloudinaryService {
    private final Cloudinary cloudinary;

    public CloudinaryServiceImpl(@Value("${cloudinary.cloud_name}") String name,
                             @Value("${cloudinary.api_key}") String key,
                             @Value("${cloudinary.api_secret}") String secret) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", name,
                "api_key", key,
                "api_secret", secret,
                "secure", true));
    }

    @Override
    public String uploadFile(MultipartFile file, String folderName) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap("folder", folderName,
                        "resource_type", "auto"));

        return uploadResult.get("secure_url").toString();
    }
}
