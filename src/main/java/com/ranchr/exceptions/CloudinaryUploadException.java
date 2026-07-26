package com.ranchr.exceptions;

import java.io.IOException;

public class CloudinaryUploadException extends RuntimeException {
	public CloudinaryUploadException(String message, IOException e) {
		super(message, e);
	}
}
