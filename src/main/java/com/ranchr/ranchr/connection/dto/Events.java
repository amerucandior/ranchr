package com.ranchr.ranchr.connection.dto;

import java.util.UUID;

public class Events {
	public record ConnectionRequestedEvent(UUID connectionRequestId) {}
	public record ConnectionApprovedEvent(UUID connectionRequestId) {}
	public record ConnectionRejectedEvent(UUID connectionRequestId) {}
}
