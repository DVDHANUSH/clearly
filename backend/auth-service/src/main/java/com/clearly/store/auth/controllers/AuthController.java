package com.clearly.store.auth.controllers;

import com.clearly.store.auth.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.security.Principal;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) { this.authService = authService; }

    @Operation(summary = "Create an account and send a verification OTP", tags = {"Account Registration"})
    @PostMapping("/register") public Object register(@Valid @RequestBody RegisterRequest request) { return authService.register(request.name(), request.email(), request.phone(), request.password(), request.channel()); }
    @Operation(summary = "Verify signup OTP and issue a JWT", tags = {"Account Registration"})
    @PostMapping("/verify-otp") public Object verifyOtp(@Valid @RequestBody OtpRequest request) { return authService.verifyOtp(request.identifier(), request.channel(), request.otp()); }
    @Operation(summary = "Resend signup OTP", tags = {"Account Registration"})
    @PostMapping("/resend-otp") public Object resendOtp(@Valid @RequestBody OtpDestinationRequest request) { return authService.resendOtp(request.identifier(), request.channel()); }
    @Operation(summary = "Sign in with email and password", tags = {"Authentication"})
    @PostMapping("/login") public Object login(@Valid @RequestBody LoginRequest request) { return authService.login(request.identifier() == null || request.identifier().isBlank() ? request.email() : request.identifier(), request.password()); }
    @Operation(summary = "Sign in with a verified Google ID token", tags = {"Authentication"})
    @PostMapping("/google") public Object google(@Valid @RequestBody GoogleRequest request) { return authService.googleLogin(request.credential()); }
    @Operation(summary = "Get the signed-in user's profile", tags = {"User Profile"})
    @GetMapping("/me") public Object me(Principal principal) { return authService.profile(principal.getName()); }
    @Operation(summary = "List registered users", description = "Returns safe account fields only. Password and OTP hashes are never included.", tags = {"Admin Users"})
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/users") public Object users(@RequestParam(defaultValue="") String search, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size) { return authService.users(search, page, size); }
    @Operation(summary = "Read public sign-in configuration", tags = {"Authentication"})
    @GetMapping("/config") public Object config() { return authService.publicConfig(); }
    @PostMapping("/logout") public Map<String, Object> logout() { return Map.of("success", true); }
    @GetMapping("/health") public Map<String,String> health() { return Map.of("service", "auth-service", "status", "UP"); }

    public record RegisterRequest(@NotBlank @Size(min=2,max=120) String name, String email, String phone, @NotBlank @Size(min=8,max=72) String password, String channel) {}
    public record LoginRequest(String email, String identifier, @NotBlank String password) {}
    public record OtpRequest(@NotBlank String identifier, String channel, @NotBlank @Size(min=4,max=4) String otp) {}
    public record OtpDestinationRequest(@NotBlank String identifier, String channel) {}
    public record GoogleRequest(@NotBlank String credential) {}
}
