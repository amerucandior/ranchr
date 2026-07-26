package com.ranchr.emails.service;

public interface EmailService {
	void sendSimpleEmail(String to, String subject, String text);
}
