package com.restaurant.gateway.controller;

import com.restaurant.gateway.dto.AuthRequest;
import com.restaurant.gateway.dto.AuthResponse;
import com.restaurant.gateway.dto.UserInfo;
import com.restaurant.gateway.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

/**
 * Authentication controller for JWT token generation
 * Provides login endpoint and token validation
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "JWT authentication endpoints")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    /**
     * Get users map with encoded passwords
     * Initialized after passwordEncoder is injected
     */
    private Map<String, String> getUsers() {
        return Map.of(
                "admin", passwordEncoder.encode("admin123"),
                "customer", passwordEncoder.encode("customer123"),
                "restaurant", passwordEncoder.encode("restaurant123")
        );
    }

    @Operation(summary = "Authenticate user and get JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid credentials",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "422", description = "Validation error")
    })
    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
        return Mono.fromCallable(() -> {
            log.info("Login attempt for user: {}", request.username());

            // Validate credentials
            Map<String, String> users = getUsers();
            String storedPassword = users.get(request.username());
            if (storedPassword == null || !passwordEncoder.matches(request.password(), storedPassword)) {
                log.warn("Invalid credentials for user: {}", request.username());
                return ResponseEntity.badRequest()
                        .body(AuthResponse.error("Invalid credentials"));
            }

            // Generate JWT token
            String role = getUserRole(request.username());
            String[] permissions = getUserPermissions(request.username());

            Map<String, Object> claims = new HashMap<>();
            claims.put("role", role);
            String token = jwtUtil.generateToken(request.username(), claims);

            // Calculate expiration time
            LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(86400); // 24 hours

            log.info("Successfully generated token for user: {}", request.username());

            AuthResponse response = AuthResponse.success(
                    token,
                    request.username(),
                    role,
                    expiresAt,
                    permissions
            );

            return ResponseEntity.ok(response);
        });
    }

    @Operation(summary = "Validate JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token validation result"),
            @ApiResponse(responseCode = "400", description = "Invalid token format")
    })
    @PostMapping("/validate")
    public Mono<ResponseEntity<Map<String, Object>>> validateToken(
            @RequestHeader("Authorization") String authorization) {
        return Mono.fromCallable(() -> {
            if (!authorization.startsWith("Bearer ")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("valid", false, "message", "Invalid token format"));
            }

            String token = authorization.substring(7);
            boolean isValid = jwtUtil.isTokenValid(token);

            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);

            if (isValid) {
                try {
                    String username = jwtUtil.getUsernameFromToken(token);
                    String role = getUserRole(username);
                    String[] permissions = getUserPermissions(username);

                    response.put("username", username);
                    response.put("role", role);
                    response.put("permissions", permissions);
                    response.put("expiresAt", jwtUtil.getExpirationDateFromToken(token)
                            .toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime());
                } catch (Exception e) {
                    log.warn("Error extracting token details: {}", e.getMessage());
                    response.put("valid", false);
                    response.put("message", "Token parsing error");
                }
            }

            return ResponseEntity.ok(response);
        });
    }

    @Operation(summary = "Get current user information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User information",
                    content = @Content(schema = @Schema(implementation = UserInfo.class))),
            @ApiResponse(responseCode = "401", description = "Invalid or expired token")
    })
    @GetMapping("/user-info")
    public Mono<ResponseEntity<UserInfo>> getUserInfo(
            @RequestHeader("Authorization") String authorization) {
        return Mono.fromCallable(() -> {
            if (!authorization.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String token = authorization.substring(7);
            if (!jwtUtil.isTokenValid(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            try {
                String username = jwtUtil.getUsernameFromToken(token);
                String role = getUserRole(username);
                String[] permissions = getUserPermissions(username);

                LocalDateTime issuedAt = jwtUtil.getClaims(token).getIssuedAt()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();

                LocalDateTime expiresAt = jwtUtil.getExpirationDateFromToken(token)
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();

                UserInfo userInfo = UserInfo.fromAuthentication(
                        username, role, permissions, issuedAt, expiresAt);

                return ResponseEntity.ok(userInfo);

            } catch (Exception e) {
                log.error("Error getting user info: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        });
    }

    @Operation(summary = "Logout user (client-side token removal)")
    @ApiResponse(responseCode = "200", description = "Logout successful")
    @PostMapping("/logout")
    public Mono<ResponseEntity<Map<String, String>>> logout() {
        return Mono.just(ResponseEntity.ok(Map.of(
                "message", "Logout successful",
                "instruction", "Remove token from client storage"
        )));
    }

    @Operation(summary = "Refresh JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid or expired token")
    })
    @PostMapping("/refresh")
    public Mono<ResponseEntity<AuthResponse>> refreshToken(
            @RequestHeader("Authorization") String authorization) {
        return Mono.fromCallable(() -> {
            if (!authorization.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(AuthResponse.error("Invalid token format"));
            }

            String token = authorization.substring(7);
            if (!jwtUtil.isTokenValid(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(AuthResponse.error("Token expired or invalid"));
            }

            try {
                String username = jwtUtil.getUsernameFromToken(token);
                String role = getUserRole(username);
                String[] permissions = getUserPermissions(username);

                // Generate new token
                Map<String, Object> claims = new HashMap<>();
                claims.put("role", role);
                String newToken = jwtUtil.generateToken(username, claims);

                LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(86400);

                log.info("Token refreshed for user: {}", username);

                AuthResponse response = AuthResponse.success(
                        newToken, username, role, expiresAt, permissions);

                return ResponseEntity.ok(response);

            } catch (Exception e) {
                log.error("Error refreshing token: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(AuthResponse.error("Token refresh failed"));
            }
        });
    }

    private String getUserRole(String username) {
        return switch (username) {
            case "admin" -> "ADMIN";
            case "restaurant" -> "RESTAURANT_OWNER";
            default -> "CUSTOMER";
        };
    }

    private String[] getUserPermissions(String username) {
        return switch (username) {
            case "admin" -> new String[]{"read", "write", "delete", "admin"};
            case "restaurant" -> new String[]{"read", "write", "manage_restaurant"};
            default -> new String[]{"read", "create_reservation"};
        };
    }
}