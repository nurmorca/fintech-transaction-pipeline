package com.fpt.code.data.record;

import java.math.BigDecimal;

public record TransactionInitiatedEvent(
        Long transactionId,
        String idempotencyKey,
        String sender,
        String receiver,
        BigDecimal amount
) {}