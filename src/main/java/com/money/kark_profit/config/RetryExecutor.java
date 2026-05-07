package com.money.kark_profit.config;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RetryExecutor {

    private final int maxAttempts;
    private final long initialDelayMillis;
    private final RetryableExceptionMatcher retryableMatcher;

    public RetryExecutor(int maxAttempts, long initialDelayMillis, RetryableExceptionMatcher retryableMatcher) {
        this.maxAttempts = maxAttempts;
        this.initialDelayMillis = initialDelayMillis;
        this.retryableMatcher = retryableMatcher;
    }

    public <T> T execute(RetryableOperation<T> operation) throws Exception {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                log.debug("Attempt {}/{}", attempt, maxAttempts);
                return operation.execute();
            } catch (Exception ex) {
                lastException = ex;

                if (!retryableMatcher.test(ex)) {
                    log.debug("Exception not retryable: {}", ex.getClass().getName());
                    throw ex;
                }

                if (attempt == maxAttempts) {
                    log.error("Max attempts ({}) reached, throwing exception", maxAttempts);
                    throw ex;
                }

                log.warn("Attempt {} failed with {}: {}. Retrying in {} ms",
                        attempt, ex.getClass().getSimpleName(), ex.getMessage(), initialDelayMillis);

                try {
                    Thread.sleep(initialDelayMillis);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted", ie);
                }
            }
        }

        throw lastException;
    }

    @FunctionalInterface
    public interface RetryableOperation<T> {
        T execute() throws Exception;
    }
}