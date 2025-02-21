package com.banquito.paymentprocessor.validamarca.banquito.exception;

public class ValidacionMarcaException extends RuntimeException {
    
    public ValidacionMarcaException(String message) {
        super(message);
    }

    public ValidacionMarcaException(String message, Throwable cause) {
        super(message, cause);
    }
} 