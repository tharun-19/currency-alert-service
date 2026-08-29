package com.tharun.currency_alert_service;

import java.math.BigDecimal;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RateController {

    private static final Logger log = LoggerFactory.getLogger(RateController.class);

    private final RateFetchService rateFetchService;

    public RateController(RateFetchService rateFetchService) {
        this.rateFetchService = rateFetchService;
    }

    @GetMapping("/rates")
    public ResponseEntity<Map<String, Object>> getRates(
            @RequestParam(defaultValue = "USD") String base,
            @RequestParam(defaultValue = "INR") String target,
            @RequestParam(defaultValue = "false") boolean fresh) {

        log.info("GET /api/rates called base={} target={} fresh={}", base, target, fresh);
        RateFetchService.RateResult result = rateFetchService.getRateDetails(base, target, fresh);

        Map<String, Object> response = Map.of(
                "base", base.toUpperCase(),
                "target", target.toUpperCase(),
                "rate", result.rate(),
                "source", result.source()
        );

        log.info("GET /api/rates response={}", response);
        return ResponseEntity.ok(response);
    }
}
