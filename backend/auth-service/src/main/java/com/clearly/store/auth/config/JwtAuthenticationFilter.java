package com.clearly.store.auth.config;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.clearly.store.auth.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    public JwtAuthenticationFilter(JwtService jwtService) { this.jwtService = jwtService; }

    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            try {
                DecodedJWT jwt = jwtService.verify(authorization.substring(7));
                String role = jwt.getClaim("role").asString();
                SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(jwt.getSubject(), null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
            } catch (Exception ignored) { SecurityContextHolder.clearContext(); }
        }
        chain.doFilter(request, response);
    }
}
