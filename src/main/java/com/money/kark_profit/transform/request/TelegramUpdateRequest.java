package com.money.kark_profit.transform.request;

import lombok.Data;

@Data
public class TelegramUpdateRequest {
    private Message message;

    @Data
    public static class Message {
        private String text;
        private Chat chat;
    }

    @Data
    public static class Chat {
        private Long id;
    }
}