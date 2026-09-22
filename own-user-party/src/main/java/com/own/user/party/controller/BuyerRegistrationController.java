package com.own.user.party.controller;

import com.own.face.trade.TradeHeaders;
import com.own.face.util.Resp;
import com.own.user.party.dto.BuyerRegistrationCommand;
import com.own.user.party.service.BuyerRegistrationService;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public onboarding endpoint; it cannot create merchant or system authority. */
@RestController
@RequestMapping("/api/v1/auth/registrations")
public class BuyerRegistrationController {
    private final BuyerRegistrationService registrations;
    public BuyerRegistrationController(BuyerRegistrationService registrations) { this.registrations = registrations; }

    @PostMapping
    public Resp register(@RequestBody BuyerRegistrationCommand command, HttpServletRequest request) {
        TradeHeaders.idempotencyKey(request);
        return new Resp(registrations.register(command), 201, "registered");
    }
}
