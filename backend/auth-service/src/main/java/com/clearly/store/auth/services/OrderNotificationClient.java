package com.clearly.store.auth.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class OrderNotificationClient {
    private static final Logger log = LoggerFactory.getLogger(OrderNotificationClient.class);
    private final ObjectMapper json;
    private final HttpClient http = HttpClient.newHttpClient();
    private final String serviceUrl;
    private final String internalKey;

    public OrderNotificationClient(ObjectMapper json,
                                   @Value("${notification.service.url}") String serviceUrl,
                                   @Value("${notification.internal-key}") String internalKey) {
        this.json = json;
        this.serviceUrl = serviceUrl.replaceAll("/+$", "");
        this.internalKey = internalKey;
    }

    @Async
    public void sendPaidOrder(long orderId, Map<String, Object> order, java.util.List<Map<String, Object>> items, String paymentId) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("orderId", orderId);
            payload.put("orderNo", order.get("order_no"));
            payload.put("invoiceNo", order.get("invoice_no"));
            payload.put("customerName", order.get("billing_name"));
            payload.put("email", order.get("billing_email"));
            payload.put("phone", order.get("billing_phone"));
            payload.put("billingAddress", order.get("billing_address"));
            payload.put("billingCity", order.get("billing_city"));
            payload.put("billingState", order.get("billing_state"));
            payload.put("billingPostalCode", order.get("billing_postal_code"));
            payload.put("items", items);
            payload.put("subtotal", order.get("subtotal"));
            payload.put("shippingAmount", order.get("shipping_amount"));
            payload.put("amount", order.get("total_amount"));
            payload.put("currency", order.getOrDefault("currency", "INR"));
            payload.put("paymentId", paymentId);
            payload.put("paidAt", order.get("paid_at"));
            HttpRequest request = HttpRequest.newBuilder(URI.create(serviceUrl + "/api/notifications/order-paid"))
                .header("Content-Type", "application/json")
                .header("X-Internal-Key", internalKey)
                .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(payload)))
                .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 202) throw new IllegalStateException("notification service returned HTTP " + response.statusCode());
            log.info("Paid-order notification accepted for {}", order.get("order_no"));
        } catch (Exception error) {
            log.error("Paid-order notification dispatch failed for {}: {}", order.get("order_no"), error.getMessage());
        }
    }

    public void sendContactEnquiry(Map<String, Object> enquiry) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(serviceUrl + "/api/notifications/contact-enquiry"))
            .header("Content-Type", "application/json")
            .header("X-Internal-Key", internalKey)
            .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(enquiry)))
            .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 202) throw new IllegalStateException("notification service returned HTTP " + response.statusCode());
    }

    public void sendContactOtp(String phone, String otp) throws Exception {
        Map<String, Object> payload = Map.of("phone", phone, "otp", otp);
        HttpRequest request = HttpRequest.newBuilder(URI.create(serviceUrl + "/api/notifications/contact-otp"))
            .header("Content-Type", "application/json")
            .header("X-Internal-Key", internalKey)
            .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(payload)))
            .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 202) {
            String detail = json.readTree(response.body()).path("message").asText("SMS provider rejected the OTP");
            throw new IllegalStateException(detail);
        }
    }
}
