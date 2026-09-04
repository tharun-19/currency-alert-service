package com.tharun.currencyalertservice.controller;

import com.tharun.currencyalertservice.domain.AlertRule;
import com.tharun.currencyalertservice.dto.CreateAlertRequest;
import com.tharun.currencyalertservice.repository.AlertRuleRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AlertController {

    private static final Logger log = LoggerFactory.getLogger(AlertController.class);
    private final AlertRuleRepository alertRuleRepository;

    public AlertController(AlertRuleRepository alertRuleRepository) {
        this.alertRuleRepository = alertRuleRepository;
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<Map<String, Object>>> getAlerts() {
        log.info("GET /api/alerts called");

        List<AlertRule> rules = alertRuleRepository.findByStatus("ACTIVE");
        List<Map<String, Object>> alerts = rules.stream()
                .map(rule -> Map.<String, Object>of(
                        "id", rule.getId().toString(),
                        "currencyPair", rule.getCurrencyPair(),
                        "direction", rule.getDirection().name(),
                        "threshold", (Object) rule.getThreshold(),
                        "status", rule.getStatus(),
                        "userId", rule.getUserId().toString()
                ))
                .toList();

        log.info("GET /api/alerts responseCount={}", alerts.size());
        return ResponseEntity.ok(alerts);
    }

    @PostMapping("/alerts")
    public ResponseEntity<Map<String, Object>> createAlert(@RequestBody CreateAlertRequest request) {
        log.info("POST /api/alerts called with currencyPair={} direction={} threshold={}",
                request.currencyPair(), request.direction(), request.threshold());

        if (request.currencyPair() == null || request.currencyPair().isBlank()) {
            log.warn("Invalid request: currencyPair is missing");
            return ResponseEntity.badRequest().body(Map.of("error", "currencyPair is required"));
        }

        if (request.direction() == null || request.direction().isBlank()) {
            log.warn("Invalid request: direction is missing");
            return ResponseEntity.badRequest().body(Map.of("error", "direction is required (ABOVE or BELOW)"));
        }

        if (request.threshold() == null) {
            log.warn("Invalid request: threshold is missing");
            return ResponseEntity.badRequest().body(Map.of("error", "threshold is required"));
        }

        AlertRule.Direction direction;
        try {
            direction = AlertRule.Direction.valueOf(request.direction().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid direction: {}", request.direction());
            return ResponseEntity.badRequest().body(Map.of("error", "direction must be ABOVE or BELOW"));
        }

        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");

        AlertRule newRule = new AlertRule(
                request.currencyPair(),
                userId,
                direction,
                request.threshold(),
                "ACTIVE"
        );

        AlertRule savedRule = alertRuleRepository.save(newRule);
        log.info("Alert created with ruleId={} currencyPair={} direction={} threshold={}",
                savedRule.getId(), savedRule.getCurrencyPair(), savedRule.getDirection(), savedRule.getThreshold());

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.<String, Object>of(
                "id", savedRule.getId().toString(),
                "currencyPair", savedRule.getCurrencyPair(),
                "direction", savedRule.getDirection().name(),
                "threshold", (Object) savedRule.getThreshold(),
                "status", savedRule.getStatus(),
                "userId", savedRule.getUserId().toString()
        ));
    }
}