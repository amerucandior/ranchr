package com.ranchr.africastalking.dto;

public record InboundSmsDto(
        String from,
        String to,
        String text,
        String date,
        String id,
        String linkId
) {
}
