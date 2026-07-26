package com.ranchr.cloudinary.serviceimpl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.ranchr.cloudinary.service.CloudinaryService;
import com.ranchr.exceptions.CloudinaryUploadException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {

	private final Cloudinary cloudinary;

	@Override
	@SuppressWarnings("unchecked")
	public Map<String, Object> uploadFile(MultipartFile file, String folder, String publicId) {
		try {
			Map<String, Object> options = ObjectUtils.asMap(
					"folder", folder,
					"public_id", publicId,
					"resource_type", "auto",
					"type", "upload",                       // force only authenticated uploads
					"overwrite", true,
					"invalidate", true
			);
			// Raw Map cast is safe here — Cloudinary always returns Map<String, Object>
			return (Map<String, Object>) cloudinary.uploader().upload(file.getBytes(), options);
		} catch (IOException e) {
			log.error("Cloudinary upload failed: {}", e.getMessage());
			throw new CloudinaryUploadException("Upload failed: " + e.getMessage(), e);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void deleteFile(String publicId) {

		try {
			cloudinary.uploader().destroy(
					publicId,
					(Map<String, Object>) ObjectUtils.asMap("invalidate", true)
			);
		} catch (IOException e) {
			log.error("Cloudinary delete failed: {}", e.getMessage());
			throw new CloudinaryUploadException("Delete failed: " + e.getMessage(), e);
		}
	}

	@Override
	public String generateSecureUrl(String publicId) {
		return cloudinary.url().secure(true).generate(publicId);
	}
}
