package com.clearly.store.catalog.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.JWTVerifier;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAdminFilter extends OncePerRequestFilter {
    private final JWTVerifier verifier;
    public JwtAdminFilter(@Value("${auth.jwt.secret}") String secret){this.verifier=JWT.require(Algorithm.HMAC256(secret)).withIssuer("clearly-auth").build();}
    @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain)throws ServletException,IOException{String value=request.getHeader("Authorization");if(value!=null&&value.startsWith("Bearer "))try{var jwt=verifier.verify(value.substring(7));String role=jwt.getClaim("role").asString();SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(jwt.getSubject(),null,List.of(new SimpleGrantedAuthority("ROLE_"+role))));}catch(Exception ignored){SecurityContextHolder.clearContext();}chain.doFilter(request,response);}
}
