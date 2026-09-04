package com.tharun.currencyalertservice.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExchangeRateResponse {

    @JsonProperty("conversion_rate")
    private Double conversionRate;

    @JsonProperty("conversion_rates")
    private Map<String, Double> conversionRates;

    public Double getConversionRate() {
        return conversionRate;
    }

    public void setConversionRate(Double conversionRate) {
        this.conversionRate = conversionRate;
    }

    public Map<String, Double> getConversionRates() {
        return conversionRates;
    }

    public void setConversionRates(Map<String, Double> conversionRates) {
        this.conversionRates = conversionRates;
    }

    public Double getConversionRateFor(String targetCurrency) {
        if (conversionRate != null) {
            return conversionRate;
        }
        if (conversionRates == null) {
            return null;
        }
        return conversionRates.get(targetCurrency.toUpperCase());
    }
}