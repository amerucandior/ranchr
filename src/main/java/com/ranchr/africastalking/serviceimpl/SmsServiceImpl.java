package com.ranchr.africastalking.serviceimpl;

import com.africastalking.AfricasTalking;
import com.africastalking.sms.Recipient;
import com.ranchr.africastalking.config.AfricasTalkingProperties;
import com.ranchr.africastalking.service.SmsService;
import com.ranchr.exceptions.SmsDeliveryException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsServiceImpl implements SmsService {

	private final AfricasTalkingProperties properties;

	@Override
	public void send(String phoneNumber, String message) {
		sendBulk(message, List.of(phoneNumber));
	}

	/**
	 * Bulk send — the actual use case the AT API is built for
	 */
	public void sendBulk(String message, List<String> phoneNumbers) {
		com.africastalking.SmsService atSms =
				AfricasTalking.getService(AfricasTalking.SERVICE_SMS);

		String[] recipients = phoneNumbers.toArray(new String[0]);

		try {
			// enqueue=true: let AT queue/send async on their side
			List<Recipient> response = atSms.send(
					message,
					properties.getSenderId(),
					recipients,
					true
			);

			for (Recipient r : response) {
				logResult(r);
			}

		} catch (Exception ex) {
			log.error("[SMS] Bulk send failed for {} recipients", recipients.length, ex);
			throw new SmsDeliveryException("Failed to send bulk SMS", ex);
		}
	}

	@Override
	public void logResult(Recipient r) {
		try {
			SmsStatusCode code = SmsStatusCode.fromCode(r.statusCode);

			if ("Success".equalsIgnoreCase(r.status)) {
				log.info(
						"[SMS] Sent to {} | {} ({}) | messageId={}",
						r.number,
						code,
						code.getDescription(),
						r.messageId
				);
			} else {
				log.error(
						"[SMS] Failed to {} | {} ({}) | AT status='{}'",
						r.number,
						code,
						code.getDescription(),
						r.status
				);
			}

		} catch (IllegalArgumentException ex) {
			log.error(
					"[SMS] Failed to {} | Unknown status code={} | status={}",
					r.number,
					r.statusCode,
					r.status
			);
		}
	}
}
