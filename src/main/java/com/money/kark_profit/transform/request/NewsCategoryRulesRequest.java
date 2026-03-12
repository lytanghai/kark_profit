package com.money.kark_profit.transform.request;

import java.util.List;
import java.util.Map;

class NewsCategoryRulesRequest {
    public static final List<CategoryRuleRequest> RULES = List.of(
        new CategoryRuleRequest("WAR", Map.of(
            "war", 5, "missile", 4, "attack", 3, "military", 3, "invasion", 5
        )),
        new CategoryRuleRequest("CRIME", Map.of(
            "murder", 5, "shooting", 4, "police", 2, "arrest", 2, "fraud", 3
        )),
        new CategoryRuleRequest("GOLD", Map.of(
            "gold", 5, "bullion", 4, "precious metal", 3
        )),
        new CategoryRuleRequest("US_ECONOMIC", Map.of(
            "fed",5,"federal reserve",5,"inflation",4,"cpi",4,"gdp",4,"interest rate",5,"unemployment",3
        )),
        new CategoryRuleRequest("OIL", Map.of(
            "oil",4,"crude",4,"opec",5,"energy market",3
        ))
    );
}