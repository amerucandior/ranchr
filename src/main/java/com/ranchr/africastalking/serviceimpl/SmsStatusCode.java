package com.ranchr.africastalking.serviceimpl;

public enum SmsStatusCode {


	PROCESSED(100, "SMS has been processed"),
	SENT(101, "SMS accepted and sent"),
	QUEUED(102, "SMS queued"),

	RISK_HOLD(401, "Held for fraud/risk checks"),
	INVALID_SENDER_ID(402, "Invalid sender ID"),
	INVALID_PHONE_NUMBER(403, "Invalid phone number"),
	UNSUPPORTED_NUMBER_TYPE(404, "Unsupported phone number type"),
	INSUFFICIENT_BALANCE(405, "Insufficient SMS balance"),
	USER_IN_BLACKLIST(406, "Recipient is blacklisted"),
	COULD_NOT_ROUTE(407, "Could not route to recipient network"),
	DND_REJECTION(409, "Rejected due to DND"),

	INTERNAL_SERVER_ERROR(500, "Internal server error"),
	GATEWAY_ERROR(501, "Gateway error"),
	REJECTED_BY_GATEWAY(502, "Rejected by gateway");

	private final int code;
	private final String description;

	SmsStatusCode(int code, String description) {
		this.code = code;
		this.description = description;
	}


	public int getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}

	public static SmsStatusCode fromCode(int code) {
		for (SmsStatusCode s : values()) {
			if (s.code == code) {
				return s;
			}
		}
		throw new IllegalArgumentException("Unknown AT status code: " + code);
	}

}
