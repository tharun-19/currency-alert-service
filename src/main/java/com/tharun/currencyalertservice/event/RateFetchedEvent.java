package com.tharun.currencyalertservice.event;

import java.math.BigDecimal;

public record RateFetchedEvent(String currencyPair, BigDecimal rate, String timestamp) {
}