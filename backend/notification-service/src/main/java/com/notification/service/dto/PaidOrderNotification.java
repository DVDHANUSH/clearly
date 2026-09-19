package com.notification.service.dto;

import java.math.BigDecimal;

public record PaidOrderNotification(
    long orderId,
    String orderNo,
    String customerName,
    String email,
    String phone,
    BigDecimal amount,
    String currency,
    String paymentId
) {}
