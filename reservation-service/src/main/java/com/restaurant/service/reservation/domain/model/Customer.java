package com.restaurant.service.reservation.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Customer domain entity representing a restaurant customer
 * Contains customer information and validation rules
 */
@Getter
public class Customer {

    private Long id;
    
    @Setter
    private String email;
    
    @Setter
    private String firstName;
    
    @Setter
    private String lastName;
    
    @Setter
    private String phoneNumber;
    
    private final LocalDateTime createdAt;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^(\\+33|0)[1-9]\\d{8}$"
    );

    /**
     * Creates a new customer
     */
    public Customer(String email, String firstName, String lastName, String phoneNumber) {
        validateConstructorParams(email, firstName, lastName, phoneNumber);
        
        this.email = email.trim().toLowerCase();
        this.firstName = firstName.trim();
        this.lastName = lastName.trim();
        this.phoneNumber = phoneNumber.trim();
        this.createdAt = LocalDateTime.now();
    }

    // Constructor for JPA
    protected Customer() {
        this.createdAt = LocalDateTime.now();
    }

    private void validateConstructorParams(String email, String firstName, String lastName, String phoneNumber) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be null or empty");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be null or empty");
        }
        if (phoneNumber != null && !phoneNumber.trim().isEmpty() && !PHONE_PATTERN.matcher(phoneNumber.trim()).matches()) {
            throw new IllegalArgumentException("Invalid phone number format");
        }
    }

    /**
     * Gets the full name of the customer
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Updates customer information
     */
    public void updatePersonalInfo(String firstName, String lastName, String phoneNumber) {
        if (firstName != null && !firstName.trim().isEmpty()) {
            this.firstName = firstName.trim();
        }
        if (lastName != null && !lastName.trim().isEmpty()) {
            this.lastName = lastName.trim();
        }
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            if (!PHONE_PATTERN.matcher(phoneNumber.trim()).matches()) {
                throw new IllegalArgumentException("Invalid phone number format");
            }
            this.phoneNumber = phoneNumber.trim();
        }
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return Objects.equals(email, customer.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    @Override
    public String toString() {
        return String.format("Customer{id=%d, email='%s', name='%s'}",
                id, email, getFullName());
    }
}