package com.money.kark_profit.transform.response;

import lombok.Data;

@Data
public class NewsArticleResponse {
    private String source;
    private String title;
    private String description;
    private String link;
    private String publishedAt;
    private String category;
    private String impact;
    private String[] marketTags;
}