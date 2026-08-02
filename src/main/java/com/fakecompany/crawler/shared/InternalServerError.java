package com.fakecompany.crawler.shared;

public class InternalServerError extends BaseException {

    private static final String CODE = "BRQ001";
    private static final String MESSAGE = "Internal server: %s";

    public InternalServerError() {
        super(CODE, buildMessage("UNKNOWN"));
    }

    public InternalServerError(String details) {
        super(CODE, buildMessage(details));
    }

    public InternalServerError(String details, Exception innerException) {
        super(CODE, buildMessage(details), innerException);
    }

    private static String buildMessage(String details) {
        return String.format(MESSAGE, details);
    }
}
