package com.notification.service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification.service.dto.PaidOrderNotification;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class PaidOrderNotificationService {
    private static final Logger log = LoggerFactory.getLogger(PaidOrderNotificationService.class);
    private final JavaMailSender mailSender;
    private final ObjectMapper json;
    private final HttpClient http = HttpClient.newHttpClient();
    private final boolean emailEnabled;
    private final String emailFrom;
    private final String accountSid;
    private final String authToken;
    private final boolean smsEnabled;
    private final String smsFrom;
    private final boolean whatsappEnabled;
    private final String whatsappFrom;
    private final String whatsappContentSid;
    private final String defaultCountryCode;

    public PaidOrderNotificationService(
        JavaMailSender mailSender,
        ObjectMapper json,
        @Value("${notification.email.enabled:false}") boolean emailEnabled,
        @Value("${notification.email.from:no-reply@clearly.local}") String emailFrom,
        @Value("${notification.twilio.account-sid:}") String accountSid,
        @Value("${notification.twilio.auth-token:}") String authToken,
        @Value("${notification.twilio.sms.enabled:false}") boolean smsEnabled,
        @Value("${notification.twilio.sms.from:}") String smsFrom,
        @Value("${notification.twilio.whatsapp.enabled:false}") boolean whatsappEnabled,
        @Value("${notification.twilio.whatsapp.from:}") String whatsappFrom,
        @Value("${notification.twilio.whatsapp.content-sid:}") String whatsappContentSid,
        @Value("${notification.phone.default-country-code:+91}") String defaultCountryCode
    ) {
        this.mailSender = mailSender;
        this.json = json;
        this.emailEnabled = emailEnabled;
        this.emailFrom = emailFrom;
        this.accountSid = accountSid;
        this.authToken = authToken;
        this.smsEnabled = smsEnabled;
        this.smsFrom = smsFrom;
        this.whatsappEnabled = whatsappEnabled;
        this.whatsappFrom = whatsappFrom;
        this.whatsappContentSid = whatsappContentSid;
        this.defaultCountryCode = defaultCountryCode;
    }

    @Async
    public void send(PaidOrderNotification order) {
        sendChannel("email", emailEnabled && hasText(order.email()), () -> sendEmail(order));
        sendChannel("sms", smsEnabled && hasText(order.phone()), () -> sendSms(order));
        sendChannel("whatsapp", whatsappEnabled && hasText(order.phone()), () -> sendWhatsapp(order));
    }

    private void sendEmail(PaidOrderNotification order) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailFrom);
        message.setTo(order.email());
        message.setSubject("Payment received for order " + order.orderNo());
        message.setText(messageBody(order));
        mailSender.send(message);
    }

    private void sendSms(PaidOrderNotification order) throws Exception {
        requireTwilio(smsFrom, "TWILIO_SMS_FROM");
        Map<String, String> form = new LinkedHashMap<>();
        form.put("From", smsFrom);
        form.put("To", phone(order.phone()));
        form.put("Body", shortMessage(order));
        sendTwilio(form);
    }

    private void sendWhatsapp(PaidOrderNotification order) throws Exception {
        requireTwilio(whatsappFrom, "TWILIO_WHATSAPP_FROM");
        if (!hasText(whatsappContentSid)) throw new IllegalStateException("TWILIO_WHATSAPP_CONTENT_SID is missing");
        Map<String, String> variables = Map.of(
            "1", safe(order.customerName()),
            "2", safe(order.orderNo()),
            "3", money(order)
        );
        Map<String, String> form = new LinkedHashMap<>();
        form.put("From", whatsapp(whatsappFrom));
        form.put("To", whatsapp(phone(order.phone())));
        form.put("ContentSid", whatsappContentSid);
        form.put("ContentVariables", json.writeValueAsString(variables));
        sendTwilio(form);
    }

    private void sendTwilio(Map<String, String> form) throws Exception {
        requireTwilio(accountSid, "TWILIO_ACCOUNT_SID");
        requireTwilio(authToken, "TWILIO_AUTH_TOKEN");
        String basic = Base64.getEncoder().encodeToString((accountSid + ":" + authToken).getBytes(StandardCharsets.UTF_8));
        String body = form.entrySet().stream().map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue())).collect(Collectors.joining("&"));
        HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json"))
            .header("Authorization", "Basic " + basic)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            String reason = json.readTree(response.body()).path("message").asText("Twilio rejected the message");
            throw new IllegalStateException(reason);
        }
    }

    private void sendChannel(String channel, boolean enabled, ThrowingAction action) {
        if (!enabled) {
            log.info("Order notification channel {} is disabled or has no recipient", channel);
            return;
        }
        try {
            action.run();
            log.info("Order notification sent through {}", channel);
        } catch (Exception error) {
            log.error("Order notification failed through {}: {}", channel, error.getMessage());
        }
    }

    private String messageBody(PaidOrderNotification order) {
        return "Hello " + safe(order.customerName()) + ",\n\nWe received your payment of " + money(order) + " for order " + safe(order.orderNo()) + ". Your order is confirmed and is now being prepared.\n\nThank you for shopping with Clearly.";
    }

    private String shortMessage(PaidOrderNotification order) {
        return "Clearly: Payment received for order " + safe(order.orderNo()) + " (" + money(order) + "). Your order is confirmed.";
    }

    private String money(PaidOrderNotification order) { return safe(order.currency()) + " " + order.amount(); }
    private String phone(String value) { String clean = safe(value).replaceAll("[^0-9+]", ""); return clean.startsWith("+") ? clean : defaultCountryCode + clean; }
    private String whatsapp(String value) { return value.startsWith("whatsapp:") ? value : "whatsapp:" + value; }
    private String encode(String value) { return URLEncoder.encode(value, StandardCharsets.UTF_8); }
    private void requireTwilio(String value, String name) { if (!hasText(value)) throw new IllegalStateException(name + " is missing"); }
    private boolean hasText(String value) { return value != null && !value.isBlank(); }
    private String safe(String value) { return value == null ? "" : value.trim(); }

    @FunctionalInterface
    private interface ThrowingAction { void run() throws Exception; }
}
