package com.money.kark_profit.transform.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ReportResponse {

    private BigDecimal totalLoss;
    private BigDecimal totalProfit;
    private BigDecimal profit;
    private BigDecimal totalWithdrawal;
    private BigDecimal totalDeposit;
    private String mostGainedDate;
    private String mostLossDate;

    private String currency = "USD";
    private String symbol;
    private String result; //WIN|LOST|DRAW

}
