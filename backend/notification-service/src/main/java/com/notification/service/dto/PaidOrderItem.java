package com.notification.service.dto;

import java.math.BigDecimal;

public record PaidOrderItem(
    String productName,
    String productUrl,
    String imageUrl,
    int quantity,
    BigDecimal unitPrice,
    BigDecimal lineTotal
) {}
