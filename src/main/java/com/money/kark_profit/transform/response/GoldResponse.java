package com.money.kark_profit.transform.response;

import lombok.Data;

import java.util.Date;

@Data
public class GoldResponse {
    private Double price;

    private Double openingPrice;
    private Double currentPrice;
    private Double gap;
    private String trend;
    private Date date;

    private String result = "FAILED!";

}
