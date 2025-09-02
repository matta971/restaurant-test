package com.restaurant.service.reservation.infrastructure.adapter.in.web.dto;

import com.restaurant.service.reservation.domain.model.Customer;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO for customer information
 */
@Schema(description = "Customer information")
public record CustomerDto(
        
        @Schema(description = "Customer ID", example = "1")
        Long id,
        
        @Schema(description = "Email address", example = "john.doe@example.com")
        String email,
        
        @Schema(description = "First name", example = "John")
        String firstName,
        
        @Schema(description = "Last name", example = "Doe")
        String lastName,
        
        @Schema(description = "Full name", example = "John Doe")
        String fullName,
        
        @Schema(description = "Phone number", example = "+33123456789")
        String phoneNumber,
        
        @Schema(description = "When customer was created")
        LocalDateTime createdAt
        
) {
    
    /**
     * Creates a CustomerDto from a Customer domain entity
     */
    public static CustomerDto fromDomain(Customer customer) {
        return new CustomerDto(
                customer.getId(),
                customer.getEmail(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getFullName(),
                customer.getPhoneNumber(),
                customer.getCreatedAt()
        );
    }
}