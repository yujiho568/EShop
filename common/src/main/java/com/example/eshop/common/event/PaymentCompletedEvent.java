package com.example.eshop.common.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentCompletedEvent(
        String eventId,
        String orderId,
        String paymentId,
        BigDecimal amount,
        Instant occurredAt
) {
    public static PaymentCompletedEvent of(String orderId, String paymentId, BigDecimal amount) {
        return new PaymentCompletedEvent(
                UUID.randomUUID().toString(),
                orderId, paymentId, amount,
                Instant.now()
        );
    }
}
