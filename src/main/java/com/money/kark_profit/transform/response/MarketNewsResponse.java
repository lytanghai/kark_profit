package com.money.kark_profit.transform.response;

import lombok.Data;

import java.util.List;

@Data
public class MarketNewsResponse {
    private String category;
    private Integer total;
    private List<MarketNews> data;

    @Data
    public static class MarketNews {
        private String title;
        private String link;
        private String source;
    }
}
