package com.clearly.store.auth.services;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ContactOtpService {
    private static final long EXPIRY_SECONDS = 300;
    private static final long RESEND_SECONDS = 45;
    private final SecureRandom random = new SecureRandom();
    private final PasswordEncoder encoder;
    private final OrderNotificationClient notifications;
    private final Map<String, Challenge> challenges = new ConcurrentHashMap<>();

    public ContactOtpService(PasswordEncoder encoder, OrderNotificationClient notifications) {
        this.encoder = encoder;
        this.notifications = notifications;
    }

    public String issue(String phone) throws Exception {
        cleanup();
        Instant now = Instant.now();
        Challenge existing = challenges.values().stream().filter(item -> item.phone().equals(phone) && item.expiresAt().isAfter(now)).findFirst().orElse(null);
        if (existing != null && existing.createdAt().plusSeconds(RESEND_SECONDS).isAfter(now)) throw new IllegalStateException("Please wait before requesting another OTP.");
        String code = String.format("%06d", random.nextInt(1_000_000));
        String id = UUID.randomUUID().toString();
        notifications.sendContactOtp(phone, code);
        challenges.put(id, new Challenge(phone, encoder.encode(code), now, now.plusSeconds(EXPIRY_SECONDS), 0, false));
        return id;
    }

    public boolean verify(String id, String phone, String code) {
        Challenge challenge = challenges.get(id);
        if (challenge == null || challenge.expiresAt().isBefore(Instant.now()) || !challenge.phone().equals(phone)) return false;
        if (challenge.attempts() >= 5) throw new IllegalStateException("Too many attempts. Request a new OTP.");
        if (!encoder.matches(code, challenge.codeHash())) {
            challenges.put(id, challenge.withAttempts(challenge.attempts() + 1));
            return false;
        }
        challenges.put(id, challenge.verified());
        return true;
    }

    public boolean consumeVerified(String id, String phone) {
        Challenge challenge = challenges.remove(id);
        return challenge != null && challenge.isVerified() && challenge.expiresAt().isAfter(Instant.now()) && challenge.phone().equals(phone);
    }

    private void cleanup() { Instant now = Instant.now(); challenges.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now)); }

    private record Challenge(String phone, String codeHash, Instant createdAt, Instant expiresAt, int attempts, boolean isVerified) {
        Challenge withAttempts(int value) { return new Challenge(phone, codeHash, createdAt, expiresAt, value, false); }
        Challenge verified() { return new Challenge(phone, codeHash, createdAt, expiresAt, attempts, true); }
    }
}
