package com.tharun.currency_alert_service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class RateFetchService {

    private static final Logger log = LoggerFactory.getLogger(RateFetchService.class);
    private static final long CACHE_TTL_SECONDS = 60L;

    public record RateResult(BigDecimal rate, String source) {
    }

    private final RedisTemplate<String, String> redisTemplate;
    private final ExchangeRateProperties exchangeRateProperties;
    private final RestClient restClient;
    private final RateHistoryRepository rateHistoryRepository;

    public RateFetchService(RedisTemplate<String, String> redisTemplate,
                           ExchangeRateProperties exchangeRateProperties,
                           RateHistoryRepository rateHistoryRepository) {
        this.redisTemplate = redisTemplate;
        this.exchangeRateProperties = exchangeRateProperties;
        this.rateHistoryRepository = rateHistoryRepository;
        this.restClient = RestClient.builder()
                .baseUrl(exchangeRateProperties.getBaseUrl())
                .build();
    }

    public BigDecimal getRate(String baseCurrency, String targetCurrency) {
        return getRateDetails(baseCurrency, targetCurrency).rate();
    }

    public RateResult getRateDetails(String baseCurrency, String targetCurrency) {
        String key = "rate:" + baseCurrency.toUpperCase() + ":" + targetCurrency.toUpperCase();

        if (redisTemplate != null) {
            String cachedValue = redisTemplate.opsForValue().get(key);
            if (cachedValue != null) {
                BigDecimal cachedRate = new BigDecimal(cachedValue);
                log.info("Redis cache hit for key={}", key);
                return new RateResult(cachedRate, "redis");
            }
        }

        log.info("Redis cache miss for key={}, fetching from exchangerate-api", key);
        BigDecimal fetchedRate = fetchFromApi(baseCurrency, targetCurrency);

        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(key, fetchedRate.toPlainString(), Duration.ofSeconds(CACHE_TTL_SECONDS));
            log.info("Stored fresh rate in Redis key={} ttlSeconds={}", key, CACHE_TTL_SECONDS);
        }

        if (rateHistoryRepository != null) {
            insertRateHistory(baseCurrency, targetCurrency, fetchedRate);
        }
        return new RateResult(fetchedRate, "exchange-rate-api");
    }

    private BigDecimal fetchFromApi(String baseCurrency, String targetCurrency) {
        try {
            String apiKey = exchangeRateProperties.getApiKey();
            if (apiKey == null || apiKey.isBlank()) {
                throw new IllegalStateException("Missing required environment variable EXCHANGE_RATE_API_KEY. Set it before starting the app.");
            }

            String url = "/" + apiKey + "/pair/" + baseCurrency.toUpperCase() + "/" + targetCurrency.toUpperCase();
            ExchangeRateResponse response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(ExchangeRateResponse.class);

            Double conversionRate = response != null ? response.getConversionRateFor(targetCurrency) : null;
            if (conversionRate == null) {
                throw new IllegalStateException("Exchange rate response missing conversion_rate for " + baseCurrency.toUpperCase() + "->" + targetCurrency.toUpperCase());
            }

            return BigDecimal.valueOf(conversionRate);
        } catch (RestClientException | IllegalStateException ex) {
            log.error("Failed to fetch exchange rate from API for {}->{}", baseCurrency, targetCurrency, ex);
            throw ex;
        }
    }

    private void insertRateHistory(String baseCurrency, String targetCurrency, BigDecimal rate) {
        rateHistoryRepository.save(new RateHistory(
                baseCurrency.toUpperCase(),
                targetCurrency.toUpperCase(),
                rate,
                LocalDateTime.now()));
        log.info("Inserted rate history baseCurrency={} targetCurrency={} rate={}",
                baseCurrency.toUpperCase(), targetCurrency.toUpperCase(), rate);
    }
}
