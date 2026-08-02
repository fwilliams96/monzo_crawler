package com.fakecompany.crawler.shared;

public class BadRequestError extends BaseException {

    private static final String CODE = "BRQ001";
    private static final String MESSAGE = "Bad request: %s";

    public BadRequestError() {
        super(CODE, buildMessage("UNKNOWN"));
    }

    public BadRequestError(String details) {
        super(CODE, buildMessage(details));
    }

    public BadRequestError(String details, Exception innerException) {
        super(CODE, buildMessage(details), innerException);
    }

    private static String buildMessage(String details) {
        return String.format(MESSAGE, details);
    }
}
