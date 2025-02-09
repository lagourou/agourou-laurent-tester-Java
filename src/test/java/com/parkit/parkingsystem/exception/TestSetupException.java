package com.parkit.parkingsystem.exception;

public class TestSetupException extends RuntimeException {

    public TestSetupException(String message) {
        super(message);
    }

    public TestSetupException(String message, Throwable cause) {
        super(message, cause);
    }

}
