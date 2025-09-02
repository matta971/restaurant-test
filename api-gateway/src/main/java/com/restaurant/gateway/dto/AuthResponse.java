package com.restaurant.gateway.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO for authentication responses
 * Contains JWT token and user information
 */
@Schema(description = "Authentication response containing JWT token and user details")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponse(
        
        @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzUxMiJ9...")
        String token,
        
        @Schema(description = "Response message", example = "Login successful")
        String message,
        
        @Schema(description = "Authenticated username", example = "customer")
        String username,
        
        @Schema(description = "User role", example = "CUSTOMER")
        String role,
        
        @Schema(description = "Token expiration time")
        LocalDateTime expiresAt,
        
        @Schema(description = "Token type", example = "Bearer")
        String tokenType,
        
        @Schema(description = "User permissions")
        String[] permissions
        
) {
    
    /**
     * Success response constructor
     */
    public AuthResponse(String token, String message, String username) {
        this(token, message, username, null, null, "Bearer", null);
    }
    
    /**
     * Full response constructor with role and permissions
     */
    public static AuthResponse success(String token, String username, String role, 
                                     LocalDateTime expiresAt, String[] permissions) {
        return new AuthResponse(
                token, 
                "Login successful", 
                username, 
                role, 
                expiresAt, 
                "Bearer", 
                permissions
        );
    }
    
    /**
     * Error response constructor
     */
    public static AuthResponse error(String message) {
        return new AuthResponse(
                null, 
                message, 
                null, 
                null, 
                null, 
                null, 
                null
        );
    }
    
    /**
     * Check if response indicates success
     */
    public boolean isSuccess() {
        return token != null && !token.trim().isEmpty();
    }
}