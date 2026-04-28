package com.example.eshop.common.event;

import java.time.Instant;
import java.util.UUID;

public record InventoryReservedEvent(
        String eventId,
        String orderId,
        Long productId,
        int quantity,
        Instant occurredAt
) {
    public static InventoryReservedEvent of(String orderId, Long productId, int quantity) {
        return new InventoryReservedEvent(
                UUID.randomUUID().toString(),
                orderId, productId, quantity,
                Instant.now()
        );
    }
}
