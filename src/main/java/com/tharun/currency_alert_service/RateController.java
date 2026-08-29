package com.tharun.currency_alert_service;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RateController {

    private static final Logger log = LoggerFactory.getLogger(RateController.class);

    @GetMapping("/rates")
    public ResponseEntity<Map<String, Object>> getRates() {
        log.info("GET /api/rates called");
        Map<String, Object> rates = Map.of(
                "base", "USD",
                "rates", Map.of(
                        "EUR", 0.92,
                        "GBP", 0.78,
                        "JPY", 157.2
                )
        );

        log.info("GET /api/rates responseKeys={}", rates.keySet());
        return ResponseEntity.ok(rates);
    }
}
