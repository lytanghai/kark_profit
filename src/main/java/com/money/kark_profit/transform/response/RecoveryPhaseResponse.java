package com.money.kark_profit.transform.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecoveryPhaseResponse {

    private double totalDebt;
    private double totalProfit;
    private double totalLoss;
    private double recoveredAmount;
    private double remainingDebt;
    private double recoveryPercentage;

}
