package com.example.demo.repository;

public class MaxRatingsAddedException extends RuntimeException {
    public MaxRatingsAddedException() {
    }

    public MaxRatingsAddedException(String message) {
        super(message);
    }

    public MaxRatingsAddedException(String message, Throwable cause) {
        super(message, cause);
    }

    public MaxRatingsAddedException(Throwable cause) {
        super(cause);
    }

    public MaxRatingsAddedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
