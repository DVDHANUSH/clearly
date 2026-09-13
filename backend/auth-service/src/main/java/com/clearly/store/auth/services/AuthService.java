package com.clearly.store.auth.services;

import com.clearly.store.auth.repositories.AuthRepository;
import com.clearly.store.auth.repositories.AuthRepository.OtpRecord;
import com.clearly.store.auth.repositories.AuthRepository.UserAccount;
import com.clearly.store.auth.services.GoogleIdentityService.GoogleProfile;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private static final String SIGNUP_EMAIL = "SIGNUP_EMAIL";
    private static final String SIGNUP_PHONE = "SIGNUP_PHONE";
    private final AuthRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final GoogleIdentityService googleIdentityService;
    private final OtpDeliveryService otpDeliveryService;
    private final SecureRandom random = new SecureRandom();
    private final int otpExpiryMinutes;
    private final boolean exposeDevelopmentOtp;

    public AuthService(AuthRepository repository, PasswordEncoder passwordEncoder, JwtService jwtService,
                       GoogleIdentityService googleIdentityService, OtpDeliveryService otpDeliveryService,
                       @Value("${auth.otp.expiry-minutes:10}") int otpExpiryMinutes,
                       @Value("${auth.otp.dev-expose:false}") boolean exposeDevelopmentOtp) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.googleIdentityService = googleIdentityService;
        this.otpDeliveryService = otpDeliveryService;
        this.otpExpiryMinutes = otpExpiryMinutes;
        this.exposeDevelopmentOtp = exposeDevelopmentOtp;
    }

    @Transactional
    public Map<String, Object> register(String rawName, String rawEmail, String rawPhone, String password, String rawChannel) {
        String name = normalizeName(rawName);
        validatePassword(password);
        String passwordHash = passwordEncoder.encode(password);
        boolean phoneChannel = "PHONE".equalsIgnoreCase(rawChannel);
        String identifier;
        UserAccount user;
        if (phoneChannel) {
            identifier = normalizePhone(rawPhone);
            UserAccount existing = repository.findByPhone(identifier).orElse(null);
            if (existing != null && existing.phoneVerified()) throw new ResponseStatusException(HttpStatus.CONFLICT, "An account already exists for this phone number");
            if (existing == null) repository.createPhoneUser(name, identifier, passwordHash);
            else repository.refreshPendingPhoneUser(existing.id(), name, passwordHash);
            user = requireIdentity(identifier);
        } else {
            identifier = normalizeEmail(rawEmail);
            UserAccount existing = repository.findByEmail(identifier).orElse(null);
            if (existing != null && existing.emailVerified()) throw new ResponseStatusException(HttpStatus.CONFLICT, "An account already exists for this email");
            if (existing == null) repository.createLocalUser(name, identifier, passwordHash);
            else repository.refreshPendingLocalUser(existing.id(), name, passwordHash);
            user = requireIdentity(identifier);
        }
        String otp = issueOtp(user, phoneChannel ? SIGNUP_PHONE : SIGNUP_EMAIL, false);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Verification code sent");
        response.put("channel", phoneChannel ? "PHONE" : "EMAIL");
        response.put("identifier", phoneChannel ? maskPhone(identifier) : maskEmail(identifier));
        response.put("expiresInSeconds", otpExpiryMinutes * 60);
        if (exposeDevelopmentOtp) response.put("developmentOtp", otp);
        return response;
    }

    @Transactional
    public Map<String, Object> resendOtp(String rawIdentifier, String rawChannel) {
        boolean phoneChannel = "PHONE".equalsIgnoreCase(rawChannel);
        String identifier = phoneChannel ? normalizePhone(rawIdentifier) : normalizeEmail(rawIdentifier);
        UserAccount user = requireIdentity(identifier);
        if (phoneChannel ? user.phoneVerified() : user.emailVerified()) throw new ResponseStatusException(HttpStatus.CONFLICT, "This account is already verified");
        String otp = issueOtp(user, phoneChannel ? SIGNUP_PHONE : SIGNUP_EMAIL, true);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "A new verification code was sent");
        response.put("identifier", phoneChannel ? maskPhone(user.phone()) : maskEmail(user.email()));
        response.put("expiresInSeconds", otpExpiryMinutes * 60);
        if (exposeDevelopmentOtp) response.put("developmentOtp", otp);
        return response;
    }

    @Transactional
    public Map<String, Object> verifyOtp(String rawIdentifier, String rawChannel, String code) {
        boolean phoneChannel = "PHONE".equalsIgnoreCase(rawChannel);
        String identifier = phoneChannel ? normalizePhone(rawIdentifier) : normalizeEmail(rawIdentifier);
        String purpose = phoneChannel ? SIGNUP_PHONE : SIGNUP_EMAIL;
        if (code == null || !code.matches("[0-9]{4}")) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter the 4-digit verification code");
        UserAccount user = requireIdentity(identifier);
        OtpRecord otp = repository.latestOtp(user.id(), purpose).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request a new verification code"));
        if (otp.consumedAt() != null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This verification code was already used");
        if (otp.expiresAt().isBefore(Instant.now())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This verification code has expired");
        if (otp.attempts() >= 5) throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many attempts. Request a new code");
        repository.incrementOtpAttempts(otp.id());
        if (!passwordEncoder.matches(code, otp.codeHash())) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "That verification code is incorrect");
        repository.consumeOtp(otp.id());
        if (phoneChannel) repository.markPhoneVerified(user.id()); else repository.markVerified(user.id());
        UserAccount verified = requireIdentity(identifier);
        repository.markLogin(verified.id());
        return session(verified);
    }

    public Map<String, Object> login(String rawIdentifier, String password) {
        String identifier = normalizeIdentifier(rawIdentifier);
        UserAccount user = requireIdentity(identifier);
        if (!"ACTIVE".equals(user.status())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This account is not active");
        if (!user.emailVerified() && !user.phoneVerified()) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Verify your email or phone before signing in");
        if (user.passwordHash() == null || !passwordEncoder.matches(password == null ? "" : password, user.passwordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect email or password");
        }
        repository.markLogin(user.id());
        return session(user);
    }

    @Transactional
    public Map<String, Object> googleLogin(String credential) {
        GoogleProfile google = googleIdentityService.verify(credential);
        String email = normalizeEmail(google.email());
        String name = normalizeName(google.name());
        UserAccount user = repository.findByEmail(email).orElse(null);
        if (user == null) repository.createGoogleUser(name, email, google.subject());
        else if (user.googleSubject() != null && !user.googleSubject().equals(google.subject())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This email is linked to another Google account");
        } else repository.linkGoogleUser(user.id(), name, google.subject(), user.passwordHash() != null);
        UserAccount signedIn = requireUser(email);
        repository.markLogin(signedIn.id());
        Map<String, Object> response = session(signedIn);
        Map<String, Object> googleUser = publicUser(signedIn);
        googleUser.put("picture", google.picture());
        response.put("user", googleUser);
        return response;
    }

    public Map<String, Object> profile(String identity) { return publicUser(requireIdentity(normalizeIdentifier(identity))); }

    public Map<String, Object> users(String search, int page, int size) {
        int safePage = Math.max(0, page), safeSize = Math.min(100, Math.max(10, size));
        List<Map<String, Object>> users = repository.findUsers(search, safeSize, safePage * safeSize).stream().map(this::publicUser).toList();
        long total = repository.countUsers(search);
        return Map.of("users", users, "total", total, "page", safePage, "size", safeSize, "pages", (total + safeSize - 1) / safeSize,
            "summary", Map.of("registered", repository.countUsers(""), "verified", repository.countVerifiedUsers(), "google", repository.countGoogleUsers()));
    }

    public Map<String, Object> publicConfig() {
        return Map.of("googleEnabled", googleIdentityService.enabled(), "googleClientId", googleIdentityService.clientId());
    }

    private String issueOtp(UserAccount user, String purpose, boolean enforceCooldown) {
        OtpRecord latest = repository.latestOtp(user.id(), purpose).orElse(null);
        if (enforceCooldown && latest != null && Duration.between(latest.createdAt(), Instant.now()).getSeconds() < 60) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Please wait one minute before requesting another code");
        }
        repository.removeOpenOtps(user.id(), purpose);
        String code = String.format("%04d", random.nextInt(10_000));
        repository.createOtp(user.id(), purpose, passwordEncoder.encode(code), Instant.now().plusSeconds(otpExpiryMinutes * 60L));
        if (SIGNUP_PHONE.equals(purpose)) otpDeliveryService.sendPhoneOtp(user.phone(), code, otpExpiryMinutes);
        else otpDeliveryService.sendSignupOtp(user.email(), user.name(), code, otpExpiryMinutes);
        return code;
    }

    private Map<String, Object> session(UserAccount user) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("accessToken", jwtService.issue(user));
        response.put("tokenType", "Bearer");
        response.put("expiresIn", jwtService.expirySeconds());
        response.put("user", publicUser(user));
        return response;
    }

    private Map<String, Object> publicUser(UserAccount user) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", user.id()); data.put("name", user.name()); data.put("email", user.email()); data.put("phone", user.phone());
        data.put("emailVerified", user.emailVerified()); data.put("phoneVerified", user.phoneVerified()); data.put("provider", user.provider()); data.put("role", user.role());
        data.put("createdAt", user.createdAt()); data.put("lastLoginAt", user.lastLoginAt());
        return data;
    }

    private UserAccount requireUser(String email) {
        return repository.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect email or password"));
    }
    private UserAccount requireIdentity(String identifier) {
        return repository.findByIdentifier(identifier).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect email, phone number, or password"));
    }
    private String normalizeIdentifier(String value) {
        String candidate = value == null ? "" : value.trim();
        return candidate.contains("@") ? normalizeEmail(candidate) : normalizePhone(candidate);
    }
    private String normalizeEmail(String email) {
        String value = email == null ? "" : email.trim().toLowerCase();
        if (!value.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a valid email address");
        return value;
    }
    private String normalizeName(String name) {
        String value = name == null ? "" : name.trim().replaceAll("\\s+", " ");
        if (value.length() < 2 || value.length() > 120) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter your full name");
        return value;
    }
    private void validatePassword(String password) {
        if (password == null || password.length() < 8 || password.length() > 72 || !password.matches(".*[A-Za-z].*") || !password.matches(".*[0-9].*")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use 8–72 characters with at least one letter and one number");
        }
    }
    private String maskEmail(String email) {
        int at = email.indexOf('@'); String local = email.substring(0, at);
        return local.substring(0, 1) + "***" + email.substring(at);
    }
    private String normalizePhone(String phone) {
        String value = phone == null ? "" : phone.replaceAll("[^0-9+]", "");
        if (value.matches("[0-9]{10}")) value = "+91" + value;
        if (!value.matches("\\+[1-9][0-9]{9,14}")) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a valid phone number with country code");
        return value;
    }
    private String maskPhone(String phone) {
        return phone.substring(0, Math.min(3, phone.length())) + "******" + phone.substring(phone.length() - 3);
    }
}
