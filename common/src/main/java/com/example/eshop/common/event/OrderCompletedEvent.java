package com.example.eshop.common.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderCompletedEvent(
        String eventId,
        String orderId,
        String memberId,
        BigDecimal totalAmount,
        Instant occurredAt
) {
    public static OrderCompletedEvent of(String orderId, String memberId, BigDecimal totalAmount) {
        return new OrderCompletedEvent(
                UUID.randomUUID().toString(),
                orderId, memberId, totalAmount,
                Instant.now()
        );
    }
}
