package com.tharun.currencyalertservice.event;

import java.math.BigDecimal;
import java.util.UUID;

public record AlertTriggeredEvent(
        UUID ruleId,
        UUID userId,
        String currencyPair,
        BigDecimal threshold,
        BigDecimal actualRate,
        String direction,
        String timestamp
) {
}