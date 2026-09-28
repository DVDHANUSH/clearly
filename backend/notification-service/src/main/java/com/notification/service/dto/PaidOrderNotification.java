package com.notification.service.dto;

import java.math.BigDecimal;
import java.util.List;

public record PaidOrderNotification(
    long orderId,
    String orderNo,
    String invoiceNo,
    String customerName,
    String email,
    String phone,
    String billingAddress,
    String billingCity,
    String billingState,
    String billingPostalCode,
    List<PaidOrderItem> items,
    BigDecimal subtotal,
    BigDecimal shippingAmount,
    BigDecimal amount,
    String currency,
    String paymentId,
    String paidAt
) {}
