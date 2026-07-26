package com.ranchr.africastalking.config;

import com.africastalking.AfricasTalking;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AfricasTalkingInitializer {

	private final AfricasTalkingProperties properties;

	@EventListener(ApplicationReadyEvent.class)
	public void init() {
		AfricasTalking.initialize(properties.getUsername(), properties.getApiKey());
	}
}
