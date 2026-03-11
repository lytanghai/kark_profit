package com.money.kark_profit.transform.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MonthlyPnLResponseDto {

    private Integer year;
    private Integer month;
    private List<DailyPnLDto> days;


    @Data
    @AllArgsConstructor
    public static class DailyPnLDto {

        private Integer day;
        private Double pnl;

    }
}
