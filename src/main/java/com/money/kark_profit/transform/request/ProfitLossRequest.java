package com.money.kark_profit.transform.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
public class ProfitLossRequest {
    private String symbol;
    private String currency;
    @JsonProperty("lot_size")
    private Double lotSize;
    private Double pnl;

    //for delete
    private Integer sn;

    //for listing
    private Date date;
    private Integer page;
    private Integer size = 10;
}
