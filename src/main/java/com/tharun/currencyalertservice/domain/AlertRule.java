package com.tharun.currencyalertservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "alert_rules")
public class AlertRule {

    public enum Direction {
        ABOVE,
        BELOW
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "currency_pair", nullable = false)
    private String currencyPair;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Direction direction;

    @Column(nullable = false)
    private BigDecimal threshold;

    @Column(nullable = false)
    private String status;

    @Column(name = "triggered_at")
    private Instant triggeredAt;

    protected AlertRule() {
    }

    public AlertRule(UUID id, String currencyPair, UUID userId, Direction direction, BigDecimal threshold, String status) {
        this.id = id;
        this.currencyPair = currencyPair;
        this.userId = userId;
        this.direction = direction;
        this.threshold = threshold;
        this.status = status;
    }

    public AlertRule(String currencyPair, UUID userId, Direction direction, BigDecimal threshold, String status) {
        this(null, currencyPair, userId, direction, threshold, status);
    }

    public UUID getId() {
        return id;
    }

    public String getCurrencyPair() {
        return currencyPair;
    }

    public UUID getUserId() {
        return userId;
    }

    public Direction getDirection() {
        return direction;
    }

    public BigDecimal getThreshold() {
        return threshold;
    }

    public String getStatus() {
        return status;
    }

    public Instant getTriggeredAt() {
        return triggeredAt;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTriggeredAt(Instant triggeredAt) {
        this.triggeredAt = triggeredAt;
    }
}