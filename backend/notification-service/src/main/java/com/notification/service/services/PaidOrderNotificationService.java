package com.notification.service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification.service.dto.PaidOrderItem;
import com.notification.service.dto.PaidOrderNotification;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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
    private final String emailSenderName;
    private final String emailReplyTo;
    private final String accountSid;
    private final String authToken;
    private final boolean smsEnabled;
    private final String smsFrom;
    private final boolean whatsappEnabled;
    private final String whatsappFrom;
    private final String whatsappContentSid;
    private final String defaultCountryCode;
    private final String storefrontUrl;
    private final String contactEnquiryTo;

    public PaidOrderNotificationService(
        JavaMailSender mailSender,
        ObjectMapper json,
        @Value("${notification.email.enabled:false}") boolean emailEnabled,
        @Value("${notification.email.from:no-reply@clearly.local}") String emailFrom,
        @Value("${notification.email.sender-name:Clearly Orders}") String emailSenderName,
        @Value("${notification.email.reply-to:}") String emailReplyTo,
        @Value("${notification.twilio.account-sid:}") String accountSid,
        @Value("${notification.twilio.auth-token:}") String authToken,
        @Value("${notification.twilio.sms.enabled:false}") boolean smsEnabled,
        @Value("${notification.twilio.sms.from:}") String smsFrom,
        @Value("${notification.twilio.whatsapp.enabled:false}") boolean whatsappEnabled,
        @Value("${notification.twilio.whatsapp.from:}") String whatsappFrom,
        @Value("${notification.twilio.whatsapp.content-sid:}") String whatsappContentSid,
        @Value("${notification.phone.default-country-code:+91}") String defaultCountryCode,
        @Value("${notification.storefront-url:http://127.0.0.1:4173}") String storefrontUrl,
        @Value("${notification.contact-enquiry-to:adhithyachem@gmail.com}") String contactEnquiryTo
    ) {
        this.mailSender = mailSender;
        this.json = json;
        this.emailEnabled = emailEnabled;
        this.emailFrom = emailFrom;
        this.emailSenderName = emailSenderName;
        this.emailReplyTo = emailReplyTo;
        this.accountSid = accountSid;
        this.authToken = authToken;
        this.smsEnabled = smsEnabled;
        this.smsFrom = smsFrom;
        this.whatsappEnabled = whatsappEnabled;
        this.whatsappFrom = whatsappFrom;
        this.whatsappContentSid = whatsappContentSid;
        this.defaultCountryCode = defaultCountryCode;
        this.storefrontUrl = storefrontUrl.replaceAll("/+$", "");
        this.contactEnquiryTo = contactEnquiryTo;
    }

    @Async
    public void send(PaidOrderNotification order) {
        sendChannel("email", emailEnabled && hasText(order.email()), () -> sendEmail(order));
        sendChannel("sms", smsEnabled && hasText(order.phone()), () -> sendSms(order));
        sendChannel("whatsapp", whatsappEnabled && hasText(order.phone()), () -> sendWhatsapp(order));
    }

    private void sendEmail(PaidOrderNotification order) {
        try {
            MimeMessageHelper message = new MimeMessageHelper(mailSender.createMimeMessage(), true, StandardCharsets.UTF_8.name());
            message.setFrom(emailFrom, emailSenderName);
            message.setTo(order.email());
            if (hasText(emailReplyTo)) message.setReplyTo(emailReplyTo);
            message.setSubject("Payment successful — Clearly order #" + safe(order.orderNo()) + " is confirmed");
            message.setText(plainTextBody(order), htmlBody(order));
            mailSender.send(message.getMimeMessage());
        } catch (Exception error) {
            throw new IllegalStateException("Could not send the order-confirmation email", error);
        }
    }

    /** Sends only a sample HTML order email; it never triggers SMS or WhatsApp. */
    public void sendTestEmail(String recipient, String customerName) {
        if (!emailEnabled) throw new IllegalStateException("Order email is disabled. Set ORDER_EMAIL_ENABLED=true and restart notification-service.");
        if (!hasText(recipient)) throw new IllegalArgumentException("A test recipient email is required");
        PaidOrderNotification sample = new PaidOrderNotification(
            0L, "CLR-DEMO-001", "INV-DEMO-001", hasText(customerName) ? customerName : "Clearly customer", recipient, "",
            "28-16-11, Ground Floor, Opp. Fire Station", "Visakhapatnam", "Andhra Pradesh", "530020",
            java.util.List.of(
                new PaidOrderItem("Shine All Lavender Floor Cleaner — 1 L", storefrontUrl + "/product.html?id=shineall-floor-cleaner-lavender", storefrontUrl + "/assets/shine-all-products/floor-cleaner-lavender/ShineAll%20Lavender%20Floor%20Cleaner.png", 2, new java.math.BigDecimal("199.00"), new java.math.BigDecimal("398.00")),
                new PaidOrderItem("Shine All Marine Toilet Cleaner — 1 L", storefrontUrl + "/product.html?id=shineall-toilet-cleaner-marine", storefrontUrl + "/assets/shine-all-products/toilet-cleaner/ShineAll%20Marine%20Fresh%20Toilet%20Cleaner%20Ad.png", 1, new java.math.BigDecimal("149.00"), new java.math.BigDecimal("149.00"))
            ),
            new java.math.BigDecimal("547.00"), java.math.BigDecimal.ZERO, new java.math.BigDecimal("547.00"), "INR", "pay_demo_success", java.time.OffsetDateTime.now().toString()
        );
        sendEmail(sample);
    }

    public void sendContactEnquiry(Map<String, Object> enquiry) {
        if (!emailEnabled) throw new IllegalStateException("Contact email is disabled. Set ORDER_EMAIL_ENABLED=true and restart notification-service.");
        if (!hasText(contactEnquiryTo)) throw new IllegalStateException("CONTACT_ENQUIRY_TO is missing");
        String name = enquiryValue(enquiry.get("name"));
        String email = enquiryValue(enquiry.get("email"));
        String phone = enquiryValue(enquiry.get("phone"));
        String topic = enquiryValue(enquiry.get("topic"));
        String orderId = enquiryValue(enquiry.get("orderId"));
        String customerMessage = enquiryValue(enquiry.get("message"));
        try {
            MimeMessageHelper message = new MimeMessageHelper(mailSender.createMimeMessage(), true, StandardCharsets.UTF_8.name());
            message.setFrom(emailFrom, emailSenderName);
            message.setTo(contactEnquiryTo);
            if (hasText(email)) message.setReplyTo(email);
            message.setSubject("New website enquiry — " + (hasText(topic) ? topic : "General enquiry"));
            String plain = "New Clearly website enquiry\n\nName: " + name + "\nEmail: " + email + "\nPhone: " + phone
                + "\nTopic: " + topic + "\nOrder ID: " + orderId + "\n\nMessage:\n" + customerMessage;
            String html = "<div style=\"font-family:Arial,sans-serif;color:#0b285c;max-width:640px;margin:auto\">"
                + "<div style=\"background:#eaf5ff;padding:28px;border-radius:18px 18px 0 0\"><small style=\"color:#4c98d1;letter-spacing:2px\">CLEARLY SUPPORT</small><h1 style=\"margin:8px 0 0\">New customer enquiry</h1></div>"
                + "<div style=\"border:1px solid #dce8f6;border-top:0;padding:28px;border-radius:0 0 18px 18px\">"
                + row("Name", name) + row("Email", email) + row("Phone", phone) + row("Topic", topic) + row("Order ID", orderId)
                + "<h3 style=\"margin:25px 0 8px\">Message</h3><div style=\"background:#f6f9fd;padding:18px;border-radius:12px;line-height:1.65\">" + html(customerMessage).replace("\n", "<br>") + "</div>"
                + "<p style=\"color:#71819d;font-size:12px;margin-top:22px\">Reply directly to this email to respond to the customer.</p></div></div>";
            message.setText(plain, html);
            Object rawAttachments = enquiry.get("attachments");
            if (rawAttachments instanceof List<?> attachments) {
                for (Object rawAttachment : attachments) {
                    if (!(rawAttachment instanceof Map<?, ?> attachment)) continue;
                    String filename = enquiryValue(attachment.get("filename"));
                    String contentType = enquiryValue(attachment.get("contentType"));
                    String data = enquiryValue(attachment.get("data"));
                    if (!hasText(filename) || !hasText(data)) continue;
                    byte[] bytes = Base64.getDecoder().decode(data);
                    message.addAttachment(filename, new ByteArrayResource(bytes), hasText(contentType) ? contentType : "application/octet-stream");
                }
            }
            mailSender.send(message.getMimeMessage());
        } catch (Exception error) {
            throw new IllegalStateException("Could not send the contact enquiry", error);
        }
    }

    private String row(String label, String value) {
        if (!hasText(value)) return "";
        return "<div style=\"display:flex;border-bottom:1px solid #edf1f6;padding:10px 0\"><b style=\"width:110px\">" + html(label) + "</b><span>" + html(value) + "</span></div>";
    }

    private String enquiryValue(Object value) { return value == null ? "" : String.valueOf(value).trim(); }

    public void sendContactOtp(String phone, String otp) throws Exception {
        if (!smsEnabled) throw new IllegalStateException("SMS delivery is disabled. Set ORDER_SMS_ENABLED=true and restart notification-service.");
        requireTwilio(smsFrom, "TWILIO_SMS_FROM");
        Map<String, String> form = new LinkedHashMap<>();
        form.put("From", smsFrom);
        form.put("To", phone(phone));
        form.put("Body", "Your Clearly enquiry verification code is " + otp + ". It expires in 5 minutes. Do not share this code.");
        sendTwilio(form);
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

    private String plainTextBody(PaidOrderNotification order) {
        StringBuilder body = new StringBuilder();
        body.append("CLEARLY\nPAYMENT SUCCESSFUL\n\nHi ").append(safe(order.customerName())).append(",\n\n")
            .append("We’ve received your payment, and your order is confirmed.\n\n")
            .append("Order number: #").append(safe(order.orderNo())).append("\n")
            .append("Invoice number: ").append(safe(order.invoiceNo())).append("\n")
            .append("Payment ID: ").append(safe(order.paymentId())).append("\n\nYOUR ITEMS\n");
        for (PaidOrderItem item : safeItems(order)) {
            body.append(item.productName()).append(" × ").append(item.quantity()).append(" — ").append(money(item.lineTotal(), order.currency())).append("\n");
        }
        body.append("\nSubtotal: ").append(money(order.subtotal(), order.currency())).append("\n")
            .append("Delivery: ").append(delivery(order)).append("\n")
            .append("Total paid: ").append(money(order.amount(), order.currency())).append("\n\n")
            .append("Expected dispatch: within 24–48 hours.\n\nThank you for shopping with Clearly.");
        return body.toString();
    }

    private String htmlBody(PaidOrderNotification order) {
        StringBuilder rows = new StringBuilder();
        for (PaidOrderItem item : safeItems(order)) {
            String productUrl = safeUrl(item.productUrl());
            String imageUrl = safeUrl(item.imageUrl());
            String title = html(item.productName());
            if (!productUrl.isBlank()) title = "<a href=\"" + productUrl + "\" style=\"color:#2563d9;text-decoration:underline;text-decoration-thickness:1px;text-underline-offset:3px;\">" + title + "</a>";
            rows.append("<tr><td style=\"padding:14px 0;border-bottom:1px solid #e6edf7;\">")
                .append(imageUrl.isBlank() ? "" : "<a href=\"" + productUrl + "\" style=\"display:inline-block;vertical-align:middle;margin-right:12px;\"><img src=\"" + imageUrl + "\" alt=\"" + html(item.productName()) + "\" width=\"56\" height=\"56\" style=\"display:block;width:56px;height:56px;object-fit:contain;border-radius:10px;background:#f4f7fb;border:1px solid #e6edf7;\"></a>")
                .append("<span style=\"display:inline-block;vertical-align:middle;max-width:310px;color:#10234a;font-weight:700;\">").append(title).append("<br><span style=\"font-size:12px;color:#6d7e9e;font-weight:400;\">Qty: ")
                .append(item.quantity()).append(" · ").append(money(item.unitPrice(), order.currency())).append(" each</span>")
                .append(productUrl.isBlank() ? "" : "<br><a href=\"" + productUrl + "\" style=\"display:inline-block;margin-top:6px;color:#2563d9;font-size:12px;font-weight:700;text-decoration:underline;text-underline-offset:2px;\">View product →</a>")
                .append("</span></td>")
                .append("<td style=\"padding:14px 0 14px 12px;border-bottom:1px solid #e6edf7;color:#10234a;font-weight:800;text-align:right;white-space:nowrap;\">")
                .append(money(item.lineTotal(), order.currency())).append("</td></tr>");
        }
        String address = html(safe(order.billingAddress())).replace("\n", "<br>") + "<br>" + html(safe(order.billingCity())) + ", " + html(safe(order.billingState())) + " – " + html(safe(order.billingPostalCode()));
        return "<!doctype html><html><body style=\"margin:0;padding:0;background:#f4f7fb;font-family:Arial,sans-serif;color:#10234a;\">"
            + "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"background:#f4f7fb;padding:28px 12px;\"><tr><td align=\"center\">"
            + "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"max-width:640px;background:#ffffff;border-radius:20px;overflow:hidden;box-shadow:0 12px 36px rgba(18,45,91,.10);\">"
            + "<tr><td style=\"padding:26px 32px;background:#0b2857;color:#ffffff;\"><div style=\"font-size:28px;font-weight:800;letter-spacing:-1px;\">Clearly<span style=\"color:#8bc4ff;\">.</span></div><div style=\"margin-top:18px;font-size:12px;font-weight:700;letter-spacing:1.2px;color:#b8e5d7;\">PAYMENT SUCCESSFUL</div><div style=\"margin-top:7px;font-size:25px;font-weight:800;\">Your order is confirmed</div></td></tr>"
            + "<tr><td style=\"padding:30px 32px 10px;\"><p style=\"margin:0 0 12px;font-size:16px;\">Hi " + html(safe(order.customerName())) + ",</p><p style=\"margin:0;color:#60718f;font-size:14px;line-height:1.65;\">Thank you for shopping with Clearly. We’ve received your payment, and your order is now confirmed.</p>"
            + "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"margin:24px 0;background:#f5f8fd;border:1px solid #e2eaf5;border-radius:12px;\"><tr><td style=\"padding:14px 16px;font-size:12px;color:#6d7e9e;\">ORDER NUMBER<br><strong style=\"color:#10234a;font-size:14px;\">#" + html(safe(order.orderNo())) + "</strong></td><td style=\"padding:14px 16px;font-size:12px;color:#6d7e9e;\">PAYMENT ID<br><strong style=\"color:#10234a;font-size:14px;\">" + html(safe(order.paymentId())) + "</strong></td></tr></table>"
            + "<h2 style=\"margin:0 0 4px;font-size:17px;\">Your items</h2><table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">" + rows + "</table>"
            + "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"margin:20px 0 26px;\"><tr><td style=\"color:#60718f;font-size:13px;line-height:1.8;\">Subtotal<br>Delivery<br><strong style=\"color:#10234a;font-size:15px;\">Total paid</strong></td><td style=\"text-align:right;color:#10234a;font-size:13px;line-height:1.8;\">" + money(order.subtotal(), order.currency()) + "<br>" + delivery(order) + "<br><strong style=\"font-size:16px;\">" + money(order.amount(), order.currency()) + "</strong></td></tr></table>"
            + "<div style=\"padding:16px;border-radius:12px;background:#effaf6;border:1px solid #d7f0e6;\"><strong style=\"font-size:14px;\">Expected dispatch: 24–48 hours</strong><br><span style=\"display:block;margin-top:5px;color:#477166;font-size:13px;line-height:1.55;\">We’ll send another update when your order is dispatched.</span></div>"
            + "<h2 style=\"margin:28px 0 8px;font-size:17px;\">Delivery address</h2><p style=\"margin:0 0 26px;color:#60718f;font-size:13px;line-height:1.65;\"><strong style=\"color:#10234a;\">" + html(safe(order.customerName())) + "</strong><br>" + address + "</p>"
            + "<p style=\"margin:0;color:#60718f;font-size:12px;line-height:1.6;\">Invoice: " + html(safe(order.invoiceNo())) + ". You can download your GST invoice from your signed-in Clearly account.</p></td></tr>"
            + "<tr><td style=\"padding:20px 32px;background:#f7f9fc;color:#71809c;font-size:11px;line-height:1.6;\">Need help? Reply to this email and our customer-support team will assist you.<br><br>Sold and billed by ADHITHYA CHEMICALS · Visakhapatnam, Andhra Pradesh<br>This is an automated order confirmation. Please keep it for your records.</td></tr>"
            + "</table></td></tr></table></body></html>";
    }

    private String shortMessage(PaidOrderNotification order) {
        return "Clearly: Payment received for order " + safe(order.orderNo()) + " (" + money(order) + "). Your order is confirmed.";
    }

    private java.util.List<PaidOrderItem> safeItems(PaidOrderNotification order) { return order.items() == null ? java.util.List.of() : order.items(); }
    private String delivery(PaidOrderNotification order) { return order.shippingAmount() == null || order.shippingAmount().signum() == 0 ? "Free" : money(order.shippingAmount(), order.currency()); }
    private String money(PaidOrderNotification order) { return money(order.amount(), order.currency()); }
    private String money(java.math.BigDecimal amount, String currency) { String symbol = "INR".equalsIgnoreCase(safe(currency)) ? "₹" : safe(currency) + " "; return symbol + (amount == null ? "0.00" : amount.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString()); }
    private String html(String value) { return safe(value).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;"); }
    private String safeUrl(String value) { String url = safe(value); return url.matches("https?://[^\\s\\\"<>]+") ? html(url) : ""; }
    private String phone(String value) { String clean = safe(value).replaceAll("[^0-9+]", ""); return clean.startsWith("+") ? clean : defaultCountryCode + clean; }
    private String whatsapp(String value) { return value.startsWith("whatsapp:") ? value : "whatsapp:" + value; }
    private String encode(String value) { return URLEncoder.encode(value, StandardCharsets.UTF_8); }
    private void requireTwilio(String value, String name) { if (!hasText(value)) throw new IllegalStateException(name + " is missing"); }
    private boolean hasText(String value) { return value != null && !value.isBlank(); }
    private String safe(String value) { return value == null ? "" : value.trim(); }

    @FunctionalInterface
    private interface ThrowingAction { void run() throws Exception; }
}
