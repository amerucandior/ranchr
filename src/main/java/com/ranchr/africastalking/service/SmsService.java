package com.ranchr.africastalking.service;

import com.africastalking.sms.Recipient;

public interface SmsService {
	void send(String phoneNumber, String message);
	void logResult(Recipient r);
}
