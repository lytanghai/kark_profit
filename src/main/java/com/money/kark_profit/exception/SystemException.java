package com.money.kark_profit.exception;

public class SystemException extends RuntimeException {

    private final String code;
    private final String message;

    public SystemException(String message) {
        super(message);
        this.code = "SYSTEM_ERROR";
        this.message = message;
    }

    public SystemException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}