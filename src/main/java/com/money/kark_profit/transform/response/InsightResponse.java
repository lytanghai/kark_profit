package com.money.kark_profit.transform.response;

import lombok.Data;

import java.util.List;

@Data
public class InsightResponse {
    private String category;
    private Integer total;
    private List<InsightNews> data;

    @Data
    public static class InsightNews {
        private String title;
        private String link;
        private String source;
    }
}
