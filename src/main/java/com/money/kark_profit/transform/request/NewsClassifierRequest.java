package com.money.kark_profit.transform.request;

import java.util.Map;

// Weighted classifier
public class NewsClassifierRequest {

    public static String classify(String text) {
        String content = text.toLowerCase();
        String bestCategory = "GENERAL";
        int bestScore = 0;

        for (CategoryRuleRequest rule : NewsCategoryRulesRequest.RULES) {
            int score = 0;
            for (Map.Entry<String,Integer> kw : rule.getKeywords().entrySet()) {
                if (content.contains(kw.getKey())) score += kw.getValue();
            }
            if (score > bestScore) {
                bestScore = score;
                bestCategory = rule.getCategory();
            }
        }
        return bestCategory;
    }

    public static String detectImpact(String category) {
        return switch (category) {
            case "US_ECONOMIC", "WAR" -> "HIGH";
            case "OIL", "GOLD" -> "MEDIUM";
            case "CRIME" -> "LOW";
            default -> "LOW";
        };
    }

    public static String[] detectMarkets(String category) {
        return switch (category) {
            case "US_ECONOMIC" -> new String[]{"USD","STOCKS"};
            case "OIL" -> new String[]{"OIL"};
            case "GOLD" -> new String[]{"GOLD"};
            case "WAR" -> new String[]{"OIL","GOLD"};
            default -> new String[]{};
        };
    }
}
