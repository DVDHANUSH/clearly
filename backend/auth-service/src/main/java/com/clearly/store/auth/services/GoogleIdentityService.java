package com.clearly.store.auth.services;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GoogleIdentityService {
    private final String clientId;
    private final GoogleIdTokenVerifier verifier;

    public GoogleIdentityService(@Value("${auth.google.client-id:}") String clientId) {
        this.clientId = clientId == null ? "" : clientId.trim();
        this.verifier = this.clientId.isBlank() ? null : new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
            .setAudience(Collections.singletonList(this.clientId)).build();
    }

    public GoogleProfile verify(String credential) {
        if (verifier == null) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Google sign-in is not configured yet");
        try {
            GoogleIdToken token = verifier.verify(credential);
            if (token == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google sign-in could not be verified");
            GoogleIdToken.Payload payload = token.getPayload();
            if (!Boolean.TRUE.equals(payload.getEmailVerified())) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google email is not verified");
            Object rawName = payload.get("name");
            Object rawPicture = payload.get("picture");
            String fallbackName = payload.getEmail().substring(0, payload.getEmail().indexOf('@'));
            return new GoogleProfile(payload.getSubject(), payload.getEmail(), rawName == null ? fallbackName : rawName.toString(), rawPicture == null ? null : rawPicture.toString());
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google sign-in could not be verified");
        }
    }

    public boolean enabled() { return verifier != null; }
    public String clientId() { return clientId; }
    public record GoogleProfile(String subject, String email, String name, String picture) {}
}
