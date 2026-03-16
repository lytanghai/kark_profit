package com.money.kark_profit.transform.request;

import lombok.Data;

import java.util.List;

@Data
public class EmailRequest {

    private From from;
    private List<To> to;
    private String subject;
    private String text;
    private String category;

    @Data
    public static class From {
        private String email;
        private String name;
    }
    @Data
    public static class To {
        private String email;
    }

}
