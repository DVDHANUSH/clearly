package com.notification.service.controllers;

import com.notification.service.dto.PaidOrderNotification;
import com.notification.service.services.PaidOrderNotificationService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class OrderNotificationController {
    private final PaidOrderNotificationService notifications;
    private final String internalKey;

    public OrderNotificationController(PaidOrderNotificationService notifications,
                                       @Value("${notification.internal-key}") String internalKey) {
        this.notifications = notifications;
        this.internalKey = internalKey;
    }

    @PostMapping("/order-paid")
    public ResponseEntity<Map<String, Object>> orderPaid(
        @RequestHeader(value = "X-Internal-Key", defaultValue = "") String suppliedKey,
        @RequestBody PaidOrderNotification order
    ) {
        if (!MessageDigest.isEqual(internalKey.getBytes(StandardCharsets.UTF_8), suppliedKey.getBytes(StandardCharsets.UTF_8))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("accepted", false));
        }
        notifications.send(order);
        return ResponseEntity.accepted().body(Map.of("accepted", true, "orderNo", order.orderNo()));
    }
}
