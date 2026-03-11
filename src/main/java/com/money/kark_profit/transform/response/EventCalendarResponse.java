package com.money.kark_profit.transform.response;

import lombok.Data;

@Data
public class EventCalendarResponse {
    private String title;
    private String country;
    private String date;
    private String impact;
    private String previous;
    private String forecast;
}
