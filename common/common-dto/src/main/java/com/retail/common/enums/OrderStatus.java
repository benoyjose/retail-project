package com.retail.common.enums;

public enum OrderStatus {
    CREATED,
    STOCK_RESERVED,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED_OUT_OF_STOCK,
    CANCELLED_PAYMENT_FAILED,
    CANCELLED_BY_CUSTOMER
}

