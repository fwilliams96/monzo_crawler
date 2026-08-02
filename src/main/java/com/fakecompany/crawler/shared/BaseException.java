package com.fakecompany.crawler.shared;

public class BaseException extends RuntimeException {

    private final String code;
    private final String message;

    public BaseException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BaseException(String code, String message, Exception innerException) {
        super(message, innerException);
        this.code = code;
        this.message = message;
    }


    @Override
    public String getMessage() {
        return message;
    }

    public String getCode() {
        return code;
    }

}
