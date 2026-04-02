package com.example.demo.controller;

import com.example.demo.model.UploadResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class UploadController {

	@Value("${app.upload.dir:uploads}")
	private String uploadDir;

	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<UploadResponse> upload(@RequestParam("file") MultipartFile file) throws IOException {
		if (file == null || file.isEmpty()) {
			return ResponseEntity.badRequest().build();
		}

		String original = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");
		if (original.contains("..")) {
			return ResponseEntity.badRequest().build();
		}

		String ext = extension(original);
		String storedName = UUID.randomUUID().toString() + (ext.isEmpty() ? "" : "." + ext);

		Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
		Files.createDirectories(dir);

		Path target = dir.resolve(storedName).normalize();
		if (!target.startsWith(dir)) {
			return ResponseEntity.badRequest().build();
		}

		Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

		String url = "/uploads/" + storedName;
		String mime = file.getContentType();
		if (mime == null || mime.isBlank()) {
			mime = "application/octet-stream";
		}

		return ResponseEntity.ok(new UploadResponse(url, mime, original));
	}

	private static String extension(String filename) {
		int i = filename.lastIndexOf('.');
		if (i < 0 || i == filename.length() - 1) {
			return "";
		}
		String ext = filename.substring(i + 1).toLowerCase(Locale.ROOT);
		if (ext.length() > 12) {
			ext = ext.substring(0, 12);
		}
		ext = ext.replaceAll("[^a-z0-9]", "");
		return ext;
	}
}
