package com.example.eshop.common.event;

import java.time.Instant;
import java.util.UUID;

public record OrderCancelledEvent(
        String eventId,
        String orderId,
        Long productId,
        int quantity,
        String reason,
        Instant occurredAt
) {
    public static OrderCancelledEvent of(String orderId, Long productId, int quantity, String reason) {
        return new OrderCancelledEvent(
                UUID.randomUUID().toString(),
                orderId, productId, quantity, reason,
                Instant.now()
        );
    }
}
