package com.clearly.store.auth.controllers;

import com.clearly.store.auth.services.ContactOtpService;
import com.clearly.store.auth.services.OrderNotificationClient;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/contact")
public class ContactEnquiryController {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PHONE = Pattern.compile("^\\+[1-9]\\d{7,14}$");
    private static final long MAX_FILE_BYTES = 5L * 1024 * 1024;
    private static final List<String> IMAGE_TYPES = List.of("image/jpeg", "image/png", "image/webp");
    private final OrderNotificationClient notifications;
    private final ContactOtpService otp;

    public ContactEnquiryController(OrderNotificationClient notifications, ContactOtpService otp) {
        this.notifications = notifications;
        this.otp = otp;
    }

    @PostMapping("/otp/send")
    public ResponseEntity<Map<String, Object>> sendOtp(@RequestBody Map<String, Object> request) {
        String phone = phone(request.get("phone"));
        if (!PHONE.matcher(phone).matches()) return bad("Enter a valid phone number with country code, for example +919791046050.");
        try {
            String id = otp.issue(phone);
            return ResponseEntity.accepted().body(Map.of("sent", true, "verificationId", id, "message", "OTP sent to " + mask(phone) + "."));
        } catch (Exception error) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("sent", false, "message", safeError(error, "OTP could not be sent right now.")));
        }
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<Map<String, Object>> verifyOtp(@RequestBody Map<String, Object> request) {
        String phone = phone(request.get("phone"));
        String id = text(request.get("verificationId"), 80);
        String code = text(request.get("otp"), 6);
        try {
            if (!code.matches("\\d{6}") || !otp.verify(id, phone, code)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("verified", false, "message", "That OTP is incorrect or expired."));
            return ResponseEntity.ok(Map.of("verified", true, "message", "Phone number verified."));
        } catch (IllegalStateException error) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of("verified", false, "message", error.getMessage()));
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> submit(@RequestPart("details") Map<String, Object> request,
                                                       @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        String name = text(request.get("name"), 120);
        String email = text(request.get("email"), 180);
        String message = text(request.get("message"), 3000);
        String verifiedPhone = phone(request.get("phone"));
        String verificationId = text(request.get("verificationId"), 80);
        if (name.isBlank() || !EMAIL.matcher(email).matches() || message.length() < 5) return bad("Please provide a valid name, email and message.");
        if (!PHONE.matcher(verifiedPhone).matches() || !otp.consumeVerified(verificationId, verifiedPhone)) return bad("Please verify your phone number before sending the enquiry.");
        if (images != null && images.size() > 3) return bad("You can attach up to 3 images.");
        try {
            Map<String, Object> enquiry = new LinkedHashMap<>();
            enquiry.put("name", name); enquiry.put("email", email); enquiry.put("phone", verifiedPhone);
            enquiry.put("topic", text(request.get("topic"), 120)); enquiry.put("orderId", text(request.get("orderId"), 100)); enquiry.put("message", message);
            List<Map<String, String>> attachments = new ArrayList<>();
            if (images != null) for (MultipartFile image : images) {
                if (image.isEmpty()) continue;
                if (image.getSize() > MAX_FILE_BYTES || !IMAGE_TYPES.contains(image.getContentType())) return bad("Attachments must be JPG, PNG or WebP images under 5 MB each.");
                String filename = image.getOriginalFilename() == null ? "enquiry-image" : image.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]", "_");
                attachments.add(Map.of("filename", filename, "contentType", image.getContentType(), "data", Base64.getEncoder().encodeToString(image.getBytes())));
            }
            enquiry.put("attachments", attachments);
            notifications.sendContactEnquiry(enquiry);
            return ResponseEntity.accepted().body(Map.of("sent", true, "message", "Your enquiry has been sent to our support team."));
        } catch (Exception error) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("sent", false, "message", "Email service is temporarily unavailable. Please email adhithyachem@gmail.com."));
        }
    }

    private ResponseEntity<Map<String, Object>> bad(String message) { return ResponseEntity.badRequest().body(Map.of("sent", false, "message", message)); }
    private String phone(Object value) { return text(value, 20).replaceAll("[\\s()-]", ""); }
    private String mask(String value) { return value.length() < 5 ? value : "••••••" + value.substring(value.length() - 4); }
    private String safeError(Exception error, String fallback) { return error.getMessage() == null || error.getMessage().isBlank() ? fallback : error.getMessage(); }
    private String text(Object value, int maximum) { String valueText = value == null ? "" : String.valueOf(value).trim(); return valueText.length() > maximum ? valueText.substring(0, maximum) : valueText; }
}
