package com.money.kark_profit.transform.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class TransactionListingResponse {
    private Integer sn;
    private String symbol;
    @JsonProperty("lot_size")
    private Double lotSize;
    private Double pnl;
    private String currency;
    private Date date;
    private String type;
    @JsonProperty("user_id")
    private Integer userId;
}
