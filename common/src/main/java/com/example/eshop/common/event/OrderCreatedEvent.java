package com.example.eshop.common.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderCreatedEvent(
        String eventId,
        String orderId,
        String memberId,
        Long productId,
        int quantity,
        BigDecimal totalAmount,
        Instant occurredAt
) {
    public static OrderCreatedEvent of(String orderId, String memberId, Long productId,
                                       int quantity, BigDecimal totalAmount) {
        return new OrderCreatedEvent(
                UUID.randomUUID().toString(),
                orderId, memberId, productId, quantity, totalAmount,
                Instant.now()
        );
    }
}
