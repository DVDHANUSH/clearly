package com.order.service.dtos;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OrderRequestDto {

    private double amount;
    private String courseId;
    private String userId;
    private String userName;
}
