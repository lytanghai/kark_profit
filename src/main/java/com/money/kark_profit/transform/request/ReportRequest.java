package com.money.kark_profit.transform.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ReportRequest {

    @JsonProperty("last_n_day")
    private Integer lastNDay = 1;

    private String symbol;
    private String type;

}
