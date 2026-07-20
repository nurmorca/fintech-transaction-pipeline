package com.fpt.code.data.record;

import java.math.BigDecimal;

public record WebhookPayload(
        Long transactionId,
        BigDecimal amount,
        String sender,
        String receiver,
        String status
) {}