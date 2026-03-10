package com.money.kark_profit.exception;

public class DatabaseException extends RuntimeException {

    private final String code;
    private final String message;

    // Constructor with default code
    public DatabaseException(String message) {
        super(message); // this sets RuntimeException#getMessage()
        this.code = "DATABASE_ERROR";
        this.message = message;
    }

    // Constructor with custom code and message
    public DatabaseException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getCustomMessage() {
        return message;
    }
}