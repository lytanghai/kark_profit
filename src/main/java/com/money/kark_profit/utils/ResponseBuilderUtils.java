package com.money.kark_profit.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ResponseBuilderUtils<R>(
        @JsonProperty("code") String code,
        @JsonProperty("message") String message,
        @JsonProperty("data") Object data) {
}
