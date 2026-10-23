package com.ranchr.africastalking.controller;

import com.ranchr.africastalking.dto.InboundSmsDto;
import com.ranchr.africastalking.service.TwoWaySmsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks/africastalking")
@RequiredArgsConstructor
@Slf4j
@Tag(
        name = "Africa's Talking Webhooks",
        description = "Inbound two-way SMS callbacks for offline field actions (accept, complete, start, status)"
)
public class SmsWebhookController {

    private final TwoWaySmsService twoWaySmsService;

    @PostMapping(
            value = "/inbound-sms",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    @Operation(summary = "Receive inbound SMS from Africa's Talking (Form Encoded)")
    public ResponseEntity<String> handleFormInboundSms(
            @RequestParam("from") String from,
            @RequestParam("to") String to,
            @RequestParam("text") String text,
            @RequestParam(value = "date", required = false) String date,
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(value = "linkId", required = false) String linkId
    ) {
        InboundSmsDto dto = new InboundSmsDto(from, to, text, date, id, linkId);
        String result = twoWaySmsService.processInboundSms(dto);
        return ResponseEntity.ok(result);
    }

    @PostMapping(
            value = "/inbound-sms",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "Receive inbound SMS from Africa's Talking (JSON)")
    public ResponseEntity<String> handleJsonInboundSms(
            @RequestBody InboundSmsDto dto
    ) {
        String result = twoWaySmsService.processInboundSms(dto);
        return ResponseEntity.ok(result);
    }
}
