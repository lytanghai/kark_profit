package com.money.kark_profit.transform.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MarketNewsRequest {

    private String category;

    @JsonProperty("last_updated")
    private String lastUpdated;
}
