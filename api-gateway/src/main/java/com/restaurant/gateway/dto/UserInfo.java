package com.restaurant.gateway.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO for user information responses
 * Contains user details extracted from JWT token
 */
@Schema(description = "User information extracted from JWT token")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserInfo(
        
        @Schema(description = "Username", example = "customer")
        String username,
        
        @Schema(description = "User role", example = "CUSTOMER")
        String role,
        
        @Schema(description = "User permissions")
        String[] permissions,
        
        @Schema(description = "Token issued at")
        LocalDateTime issuedAt,
        
        @Schema(description = "Token expires at")
        LocalDateTime expiresAt,
        
        @Schema(description = "Whether token is active")
        Boolean active
        
) {
    
    /**
     * Creates UserInfo from authentication details
     */
    public static UserInfo fromAuthentication(String username, String role, 
                                            String[] permissions, 
                                            LocalDateTime issuedAt, 
                                            LocalDateTime expiresAt) {
        boolean active = expiresAt == null || expiresAt.isAfter(LocalDateTime.now());
        
        return new UserInfo(
                username,
                role,
                permissions,
                issuedAt,
                expiresAt,
                active
        );
    }
    
    /**
     * Check if user has specific permission
     */
    public boolean hasPermission(String permission) {
        if (permissions == null || permission == null) {
            return false;
        }
        
        for (String userPermission : permissions) {
            if (permission.equals(userPermission)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if user has specific role
     */
    public boolean hasRole(String checkRole) {
        return role != null && role.equals(checkRole);
    }
}