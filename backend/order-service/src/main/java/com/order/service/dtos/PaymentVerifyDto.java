package com.order.service.dtos;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentVerifyDto {

    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;

}