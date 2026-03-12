package com.money.kark_profit.transform.request;

import java.util.Map;

public class CategoryRuleRequest {
    private String category;
    private Map<String, Integer> keywords;

    public CategoryRuleRequest(String category, Map<String, Integer> keywords) {
        this.category = category;
        this.keywords = keywords;
    }

    public String getCategory() { return category; }
    public Map<String, Integer> getKeywords() { return keywords; }
}