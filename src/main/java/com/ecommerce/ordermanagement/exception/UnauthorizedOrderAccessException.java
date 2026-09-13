package com.ecommerce.ordermanagement.exception;

public class UnauthorizedOrderAccessException extends RuntimeException {

    public UnauthorizedOrderAccessException(String message) {
        super(message);
    }
}