package com.clearly.store.auth.repositories;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AuthRepository {
    private final JdbcTemplate jdbc;

    public AuthRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Optional<UserAccount> findByEmail(String email) {
        List<UserAccount> rows = jdbc.query("SELECT * FROM users WHERE email = ?", this::mapUser, email);
        return rows.stream().findFirst();
    }

    public Optional<UserAccount> findByPhone(String phone) {
        List<UserAccount> rows = jdbc.query("SELECT * FROM users WHERE phone_number = ?", this::mapUser, phone);
        return rows.stream().findFirst();
    }

    public Optional<UserAccount> findByIdentifier(String identifier) {
        return identifier != null && identifier.startsWith("+") ? findByPhone(identifier) : findByEmail(identifier);
    }

    public List<UserAccount> findUsers(String search, int limit, int offset) {
        String query = search == null ? "" : search.trim();
        if (query.isBlank()) return jdbc.query("SELECT * FROM users ORDER BY created_at DESC LIMIT ? OFFSET ?", this::mapUser, limit, offset);
        String like = "%" + query + "%";
        return jdbc.query("SELECT * FROM users WHERE full_name LIKE ? OR email LIKE ? OR phone_number LIKE ? OR auth_provider LIKE ? OR role LIKE ? ORDER BY created_at DESC LIMIT ? OFFSET ?", this::mapUser, like, like, like, like, like, limit, offset);
    }

    public long countUsers(String search) {
        String query = search == null ? "" : search.trim();
        if (query.isBlank()) return jdbc.queryForObject("SELECT COUNT(*) FROM users", Long.class);
        String like = "%" + query + "%";
        return jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE full_name LIKE ? OR email LIKE ? OR phone_number LIKE ? OR auth_provider LIKE ? OR role LIKE ?", Long.class, like, like, like, like, like);
    }

    public long countVerifiedUsers() { return jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE email_verified = TRUE", Long.class); }
    public long countGoogleUsers() { return jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE google_subject IS NOT NULL", Long.class); }

    public void createLocalUser(String name, String email, String passwordHash) {
        jdbc.update("INSERT INTO users (email, full_name, password_hash, auth_provider, email_verified, status) VALUES (?, ?, ?, 'EMAIL', FALSE, 'PENDING')", email, name, passwordHash);
    }

    public void refreshPendingLocalUser(long id, String name, String passwordHash) {
        jdbc.update("UPDATE users SET full_name = ?, password_hash = ?, auth_provider = 'EMAIL', status = 'PENDING' WHERE id = ?", name, passwordHash, id);
    }

    public void createPhoneUser(String name, String phone, String passwordHash) {
        jdbc.update("INSERT INTO users (phone_number, full_name, password_hash, auth_provider, status) VALUES (?, ?, ?, 'PHONE', 'PENDING')", phone, name, passwordHash);
    }

    public void refreshPendingPhoneUser(long id, String name, String passwordHash) {
        jdbc.update("UPDATE users SET full_name = ?, password_hash = ?, auth_provider = 'PHONE', status = 'PENDING' WHERE id = ?", name, passwordHash, id);
    }

    public void createGoogleUser(String name, String email, String subject) {
        jdbc.update("INSERT INTO users (email, full_name, auth_provider, google_subject, email_verified) VALUES (?, ?, 'GOOGLE', ?, TRUE)", email, name, subject);
    }

    public void linkGoogleUser(long id, String name, String subject, boolean hasPassword) {
        jdbc.update("UPDATE users SET full_name = ?, google_subject = ?, auth_provider = ?, email_verified = TRUE, status = 'ACTIVE' WHERE id = ?", name, subject, hasPassword ? "LOCAL_GOOGLE" : "GOOGLE", id);
    }

    public void markVerified(long id) {
        jdbc.update("UPDATE users SET email_verified = TRUE, status = 'ACTIVE' WHERE id = ?", id);
    }


    public void markPhoneVerified(long id) {
        jdbc.update("UPDATE users SET phone_verified = TRUE, status = 'ACTIVE' WHERE id = ?", id);
    }

    public void markLogin(long id) {
        jdbc.update("UPDATE users SET last_login_at = CURRENT_TIMESTAMP WHERE id = ?", id);
    }

    public void removeOpenOtps(long userId, String purpose) {
        jdbc.update("DELETE FROM user_otp_verifications WHERE user_id = ? AND purpose = ? AND consumed_at IS NULL", userId, purpose);
    }

    public void createOtp(long userId, String purpose, String codeHash, Instant expiresAt) {
        jdbc.update("INSERT INTO user_otp_verifications (user_id, purpose, code_hash, expires_at) VALUES (?, ?, ?, ?)", userId, purpose, codeHash, Timestamp.from(expiresAt));
    }

    public Optional<OtpRecord> latestOtp(long userId, String purpose) {
        List<OtpRecord> rows = jdbc.query("SELECT id, code_hash, expires_at, attempts, consumed_at, created_at FROM user_otp_verifications WHERE user_id = ? AND purpose = ? ORDER BY created_at DESC LIMIT 1", (rs, n) -> new OtpRecord(
            rs.getLong("id"), rs.getString("code_hash"), rs.getTimestamp("expires_at").toInstant(), rs.getInt("attempts"),
            rs.getTimestamp("consumed_at") == null ? null : rs.getTimestamp("consumed_at").toInstant(), rs.getTimestamp("created_at").toInstant()
        ), userId, purpose);
        return rows.stream().findFirst();
    }

    public void incrementOtpAttempts(long otpId) { jdbc.update("UPDATE user_otp_verifications SET attempts = attempts + 1 WHERE id = ?", otpId); }
    public void consumeOtp(long otpId) { jdbc.update("UPDATE user_otp_verifications SET consumed_at = CURRENT_TIMESTAMP WHERE id = ?", otpId); }

    private UserAccount mapUser(ResultSet rs, int rowNum) throws SQLException {
        Timestamp login = rs.getTimestamp("last_login_at");
        return new UserAccount(rs.getLong("id"), rs.getString("email"), rs.getString("phone_number"), rs.getString("full_name"), rs.getString("password_hash"),
            rs.getString("auth_provider"), rs.getString("google_subject"), rs.getBoolean("email_verified"), rs.getBoolean("phone_verified"), rs.getString("role"),
            rs.getString("status"), rs.getTimestamp("created_at").toInstant(), login == null ? null : login.toInstant());
    }

    public record UserAccount(long id, String email, String phone, String name, String passwordHash, String provider, String googleSubject,
                              boolean emailVerified, boolean phoneVerified, String role, String status, Instant createdAt, Instant lastLoginAt) {}
    public record OtpRecord(long id, String codeHash, Instant expiresAt, int attempts, Instant consumedAt, Instant createdAt) {}
}
