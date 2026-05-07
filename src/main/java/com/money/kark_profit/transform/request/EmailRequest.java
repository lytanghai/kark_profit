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

    private List<Attachment> attachments;

    @Data
    public static class Attachment {
        private String filename;
        private String content;
        private String type;
        private String disposition;
    }

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
