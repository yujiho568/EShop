package com.example.eshop.common.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentFailedEvent(
        String eventId,
        String orderId,
        BigDecimal amount,
        String reason,
        Instant occurredAt
) {
    public static PaymentFailedEvent of(String orderId, BigDecimal amount, String reason) {
        return new PaymentFailedEvent(
                UUID.randomUUID().toString(),
                orderId, amount, reason,
                Instant.now()
        );
    }
}
