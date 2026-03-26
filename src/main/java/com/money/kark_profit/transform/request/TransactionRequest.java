package com.money.kark_profit.transform.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
public class TransactionRequest {
    private String symbol;
    private String currency;

    private Double pnl;
    @JsonProperty("inp_date")
    private String inpDate;

    //for delete
    private Integer sn;

    //for listing
    private Date date;
    private Integer page;
    private Integer size = 10;

    @JsonProperty("user_id")
    private Integer userId;
    private String type;

    public void setType(String type) {
        this.type = (type == null) ? null : type.toUpperCase();
    }
}
