package com.restaurant.service.reservation.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO for reservation creation and update requests
 */
@Schema(description = "Reservation request data")
public record ReservationRequestDto(
        
        @Schema(description = "Customer email address", example = "john.doe@example.com")
        @NotBlank(message = "Customer email is required")
        @Email(message = "Invalid email format")
        String customerEmail,
        
        @Schema(description = "Customer first name", example = "John")
        @NotBlank(message = "Customer first name is required")
        @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
        String customerFirstName,
        
        @Schema(description = "Customer last name", example = "Doe")
        @NotBlank(message = "Customer last name is required")
        @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
        String customerLastName,
        
        @Schema(description = "Customer phone number", example = "+33123456789")
        @Pattern(regexp = "^(\\+33|0)[1-9]\\d{8}$", message = "Invalid French phone number format")
        String customerPhoneNumber,
        
        @Schema(description = "Restaurant ID", example = "1")
        @NotNull(message = "Restaurant ID is required")
        @Positive(message = "Restaurant ID must be positive")
        Long restaurantId,
        
        @Schema(description = "Table ID", example = "1")
        @NotNull(message = "Table ID is required")
        @Positive(message = "Table ID must be positive")
        Long tableId,
        
        @Schema(description = "Reservation date", example = "2024-12-25")
        @NotNull(message = "Reservation date is required")
        @Future(message = "Reservation date must be in the future")
        LocalDate reservationDate,
        
        @Schema(description = "Start time", example = "19:00")
        @NotNull(message = "Start time is required")
        LocalTime startTime,
        
        @Schema(description = "End time", example = "21:00")
        @NotNull(message = "End time is required")
        LocalTime endTime,
        
        @Schema(description = "Number of people", example = "4")
        @NotNull(message = "Party size is required")
        @Min(value = 1, message = "Party size must be at least 1")
        @Max(value = 12, message = "Party size cannot exceed 12")
        Integer partySize,
        
        @Schema(description = "Special requests", example = "Vegetarian menu preferred")
        @Size(max = 500, message = "Special requests cannot exceed 500 characters")
        String specialRequests
        
) {
    
    public ReservationRequestDto {
        // Validate end time is after start time
        if (startTime != null && endTime != null && !endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        
        // Validate reservation duration (minimum 1 hour, maximum 4 hours)
        if (startTime != null && endTime != null) {
            long durationMinutes = java.time.Duration.between(startTime, endTime).toMinutes();
            if (durationMinutes < 60) {
                throw new IllegalArgumentException("Minimum reservation duration is 1 hour");
            }
            if (durationMinutes > 240) {
                throw new IllegalArgumentException("Maximum reservation duration is 4 hours");
            }
        }
    }
}