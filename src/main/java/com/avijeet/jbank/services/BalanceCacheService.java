package com.avijeet.jbank.services;

import com.avijeet.jbank.constants.AppConstants;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class BalanceCacheService {

    private final RedisTemplate<String, String> redisTemplate;

    public BalanceCacheService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Retry(name = "redisRetry")
    @CircuitBreaker(name = "redisCircuit", fallbackMethod = "getBalanceFallback")
    public Optional<BigDecimal> getBalance(String accountNumber) {
        String value = redisTemplate.opsForValue().get(key(accountNumber));
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(new BigDecimal(value));
    }

    public Optional<BigDecimal> getBalanceFallback(String accountNumber, Throwable throwable) {
        return Optional.empty();
    }

    @Retry(name = "redisRetry")
    @CircuitBreaker(name = "redisCircuit", fallbackMethod = "putBalanceFallback")
    public void putBalance(String accountNumber, BigDecimal balance) {
        redisTemplate.opsForValue().set(
                key(accountNumber),
                balance.toPlainString(),
                AppConstants.BALANCE_CACHE_TTL_HOURS,
                TimeUnit.HOURS
        );
    }

    public void putBalanceFallback(String accountNumber, BigDecimal balance, Throwable throwable) {
    }

    @Retry(name = "redisRetry")
    @CircuitBreaker(name = "redisCircuit", fallbackMethod = "evictBalanceFallback")
    public void evictBalance(String accountNumber) {
        redisTemplate.delete(key(accountNumber));
    }

    public void evictBalanceFallback(String accountNumber, Throwable throwable) {
    }

    private String key(String accountNumber) {
        return "balance:" + accountNumber;
    }
}

