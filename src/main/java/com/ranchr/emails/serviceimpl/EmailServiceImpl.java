package com.ranchr.emails.serviceimpl;

import com.ranchr.emails.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

	private final JavaMailSender mailSender;

	public EmailServiceImpl(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	public void sendSimpleEmail(String to, String subject, String text) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom("ourinsurance254@gmail.com");
		message.setTo(to);
		message.setSubject(subject);
		message.setText(text);

		mailSender.send(message);
		log.info("email sent to {}", to);
	}
}
