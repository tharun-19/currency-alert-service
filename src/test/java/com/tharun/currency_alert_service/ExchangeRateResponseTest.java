package com.tharun.currency_alert_service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;
import org.junit.jupiter.api.Test;

class ExchangeRateResponseTest {

    @Test
    void pairResponseUsesDirectConversionRate() {
        ExchangeRateResponse response = new ExchangeRateResponse();
        response.setConversionRate(83.5);

        assertNotNull(response.getConversionRate());
        assertEquals(83.5, response.getConversionRate());
    }

    @Test
    void latestResponseUsesNestedConversionMap() {
        ExchangeRateResponse response = new ExchangeRateResponse();
        response.setConversionRates(Map.of("INR", 83.5, "USD", 1.0));

        assertEquals(83.5, response.getConversionRateFor("INR"));
    }
}
