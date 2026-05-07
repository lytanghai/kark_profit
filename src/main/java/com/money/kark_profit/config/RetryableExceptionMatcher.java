package com.money.kark_profit.config;

import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.List;

public class RetryableExceptionMatcher {

    private final List<String> retryableExceptionTypes;

    public RetryableExceptionMatcher(List<String> retryableExceptionTypes) {
        this.retryableExceptionTypes = retryableExceptionTypes;
    }

    public boolean test(Throwable throwable) {
        if (throwable == null) return false;

        Throwable current = throwable;

        while (current != null) {
            String exceptionName = current.getClass().getName();
            String simpleName = current.getClass().getSimpleName();

            log.debug("Checking exception: {} - {}", exceptionName, simpleName);

            for (String retryableType : retryableExceptionTypes) {
                if (exceptionName.equals(retryableType) || simpleName.equals(retryableType)) {
                    log.debug("Exception {} matches retryable type: {}", simpleName, retryableType);
                    return true;
                }
            }

            if (current instanceof ResourceAccessException) {
                log.debug("Detected ResourceAccessException, checking cause");
                Throwable cause = current.getCause();
                if (cause != null) {
                    String causeName = cause.getClass().getName();
                    String causeSimpleName = cause.getClass().getSimpleName();

                    // Check if cause is retryable
                    for (String retryableType : retryableExceptionTypes) {
                        if (causeName.equals(retryableType) || causeSimpleName.equals(retryableType)) {
                            log.debug("ResourceAccessException cause {} matches retryable type: {}",
                                    causeSimpleName, retryableType);
                            return true;
                        }
                    }

                    if (cause instanceof SocketTimeoutException) {
                        if (retryableExceptionTypes.contains("java.net.SocketTimeoutException") ||
                                retryableExceptionTypes.contains("SocketTimeoutException")) {
                            log.debug("SocketTimeoutException is retryable");
                            return true;
                        }
                    }

                    if (cause instanceof ConnectException) {
                        if (retryableExceptionTypes.contains("java.net.ConnectException") ||
                                retryableExceptionTypes.contains("ConnectException")) {
                            log.debug("ConnectException is retryable");
                            return true;
                        }
                    }
                }
            }

            // Check for HTTP status code based retry
            if (current instanceof HttpClientErrorException) {
                HttpClientErrorException ex = (HttpClientErrorException) current;
                if (retryableExceptionTypes.contains("Http4xx")) {
                    int statusCode = ex.getStatusCode().value();
                    if (statusCode == 408 || statusCode == 429) { // Request Timeout or Too Many Requests
                        log.debug("HTTP 4xx status {} is retryable", statusCode);
                        return true;
                    }
                }
            }

            if (current instanceof HttpServerErrorException) {
                if (retryableExceptionTypes.contains("Http5xx")) {
                    log.debug("HTTP 5xx error is retryable");
                    return true;
                }
            }

            current = current.getCause();
        }

        log.debug("Exception {} is not retryable", throwable.getClass().getSimpleName());
        return false;
    }

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(RetryableExceptionMatcher.class);
}