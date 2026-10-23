package com.ranchr.africastalking.service;

import com.ranchr.africastalking.dto.InboundSmsDto;

public interface TwoWaySmsService {
    String processInboundSms(InboundSmsDto sms);
}
