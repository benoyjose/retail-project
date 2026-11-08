package com.retail.support.exception;

public class SupportTicketNotFoundException extends RuntimeException {
    public SupportTicketNotFoundException(String message) {
        super(message);
    }
}

