package com.ranchr.africastalking.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "africastalking")
@Getter
public class AfricasTalkingProperties {

	@Value( "${africastalking.username}")
	private String username;

	@Value( "${africastalking.api-key}")
	private String apiKey;

	@Value( "${africastalking.sender-id}")
	private String senderId;

}
