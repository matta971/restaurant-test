package com.restaurant.gateway.security;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * JWT Authentication Manager for reactive authentication
 * Creates Spring Security Authentication objects from JWT tokens
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationManager {

    private final JwtUtil jwtUtil;

    /**
     * Creates Authentication object from JWT token
     */
    public Authentication createAuthentication(String token) {
        try {
            // Extract claims from token
            Claims claims = jwtUtil.getClaims(token);
            String username = claims.getSubject();
            
            // Extract role from claims
            String role = claims.get("role", String.class);
            
            // Create authorities from role
            Collection<GrantedAuthority> authorities = createAuthorities(role);
            
            log.debug("Creating authentication for user: {} with role: {}", username, role);
            
            // Create authentication object
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    username, 
                    null, // No credentials needed for JWT
                    authorities
            );
            
            // Add additional details
            auth.setDetails(createUserDetails(claims));
            
            return auth;
            
        } catch (Exception e) {
            log.error("Failed to create authentication from JWT token: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    /**
     * Creates authorities from role string
     */
    private Collection<GrantedAuthority> createAuthorities(String role) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        if (role != null && !role.trim().isEmpty()) {
            // Add role-based authority
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
            
            // Add permissions based on role
            switch (role) {
                case "ADMIN":
                    authorities.add(new SimpleGrantedAuthority("PERMISSION_READ"));
                    authorities.add(new SimpleGrantedAuthority("PERMISSION_write"));
                    authorities.add(new SimpleGrantedAuthority("PERMISSION_delete"));
                    authorities.add(new SimpleGrantedAuthority("PERMISSION_admin"));
                    break;
                    
                case "RESTAURANT_OWNER":
                    authorities.add(new SimpleGrantedAuthority("PERMISSION_read"));
                    authorities.add(new SimpleGrantedAuthority("PERMISSION_write"));
                    authorities.add(new SimpleGrantedAuthority("PERMISSION_manage_restaurant"));
                    break;
                    
                case "CUSTOMER":
                default:
                    authorities.add(new SimpleGrantedAuthority("PERMISSION_read"));
                    authorities.add(new SimpleGrantedAuthority("PERMISSION_create_reservation"));
                    break;
            }
        }
        
        log.debug("Created authorities: {}", authorities);
        return authorities;
    }

    /**
     * Creates user details from JWT claims
     */
    private UserDetails createUserDetails(Claims claims) {
        return new UserDetails(
                claims.getSubject(),
                claims.get("role", String.class),
                claims.getIssuedAt(),
                claims.getExpiration()
        );
    }

    /**
     * User details extracted from JWT token
     */
    public static class UserDetails {
        private final String username;
        private final String role;
        private final java.util.Date issuedAt;
        private final java.util.Date expiration;

        public UserDetails(String username, String role, java.util.Date issuedAt, java.util.Date expiration) {
            this.username = username;
            this.role = role;
            this.issuedAt = issuedAt;
            this.expiration = expiration;
        }

        public String getUsername() {
            return username;
        }

        public String getRole() {
            return role;
        }

        public java.util.Date getIssuedAt() {
            return issuedAt;
        }

        public java.util.Date getExpiration() {
            return expiration;
        }

        @Override
        public String toString() {
            return String.format("UserDetails{username='%s', role='%s', issuedAt=%s, expiration=%s}",
                    username, role, issuedAt, expiration);
        }
    }
}