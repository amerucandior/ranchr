package com.ranchr.cloudinary.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface CloudinaryService {
	Map<String, Object> uploadFile(MultipartFile file, String folder, String publicId);
	void deleteFile(String publicId);
	String generateSecureUrl(String publicId);
}
