package com.example.eshop.common.event;

import java.time.Instant;
import java.util.UUID;

public record InventoryFailedEvent(
        String eventId,
        String orderId,
        Long productId,
        int requestedQuantity,
        String reason,
        Instant occurredAt
) {
    public static InventoryFailedEvent of(String orderId, Long productId, int qty, String reason) {
        return new InventoryFailedEvent(
                UUID.randomUUID().toString(),
                orderId, productId, qty, reason,
                Instant.now()
        );
    }
}
