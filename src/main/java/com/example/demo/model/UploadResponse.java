package com.example.demo.model;

public class UploadResponse {

	private String url;
	private String mimeType;
	private String originalFilename;

	public UploadResponse() {
	}

	public UploadResponse(String url, String mimeType, String originalFilename) {
		this.url = url;
		this.mimeType = mimeType;
		this.originalFilename = originalFilename;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getOriginalFilename() {
		return originalFilename;
	}

	public void setOriginalFilename(String originalFilename) {
		this.originalFilename = originalFilename;
	}
}
