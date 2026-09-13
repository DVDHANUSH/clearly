package com.clearly.store.auth.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class OtpDeliveryService {
    private static final Logger log = LoggerFactory.getLogger(OtpDeliveryService.class);
    private final JavaMailSender mailSender;
    private final boolean mailEnabled;
    private final String from;

    public OtpDeliveryService(JavaMailSender mailSender, @Value("${auth.mail.enabled:false}") boolean mailEnabled,
                              @Value("${spring.mail.username:no-reply@clearly.local}") String from) {
        this.mailSender = mailSender;
        this.mailEnabled = mailEnabled;
        this.from = from == null || from.isBlank() ? "no-reply@clearly.local" : from;
    }

    public void sendSignupOtp(String email, String name, String otp, int expiryMinutes) {
        if (!mailEnabled) {
            log.info("Development signup OTP for {} is {}", email, otp);
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("Verify your Clearly account");
        message.setText("Hello " + name + ",\n\nYour Clearly verification code is " + otp + ". It expires in " + expiryMinutes + " minutes.\n\nIf you did not request this, you can ignore this email.");
        mailSender.send(message);
    }

    public void sendPhoneOtp(String phone, String otp, int expiryMinutes) {
        // The local preview exposes this code in the UI. Connect an SMS provider
        // here for production delivery without changing the verification flow.
        log.info("Development phone OTP for {} is {} (expires in {} minutes)", phone, otp, expiryMinutes);
    }
}
