package com.example.eshop.common.event;

public final class Topics {
    public static final String ORDER_CREATED      = "order.created";
    public static final String ORDER_COMPLETED    = "order.completed";
    public static final String ORDER_CANCELLED    = "order.cancelled";
    public static final String INVENTORY_RESERVED = "inventory.reserved";
    public static final String INVENTORY_FAILED   = "inventory.failed";
    public static final String PAYMENT_COMPLETED  = "payment.completed";
    public static final String PAYMENT_FAILED     = "payment.failed";

    private Topics() {}
}
