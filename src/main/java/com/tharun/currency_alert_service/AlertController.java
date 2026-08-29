package com.tharun.currency_alert_service;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AlertController {

    private static final Logger log = LoggerFactory.getLogger(AlertController.class);

    @GetMapping("/alerts")
    public ResponseEntity<List<Map<String, Object>>> getAlerts() {
        log.info("GET /api/alerts called");
        List<Map<String, Object>> alerts = List.of(
                Map.of("id", 1, "currency", "USD/EUR", "threshold", 0.92, "enabled", true),
                Map.of("id", 2, "currency", "USD/GBP", "threshold", 0.78, "enabled", true)
        );

        log.info("GET /api/alerts responseCount={}", alerts.size());
        return ResponseEntity.ok(alerts);
    }
}
