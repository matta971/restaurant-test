package com.restaurant.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


/**
 * DTO for authentication requests
 * Contains username and password for login
 */
@Schema(description = "Authentication request containing user credentials")
public record AuthRequest(
        
        @Schema(description = "Username for authentication", example = "customer")
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,
        
        @Schema(description = "Password for authentication", example = "customer123")
        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
        String password
        
) {
    
    /**
     * Constructor with validation
     */
    public AuthRequest {
        if (username != null) {
            username = username.trim().toLowerCase();
        }
        if (password != null) {
            password = password.trim();
        }
    }
}