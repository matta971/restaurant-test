package com.restaurant.service.restaurant.infrastructure.adapter.in.web.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for request/response DTOs validation
 * Testing Bean Validation annotations and constraints
 */
@DisplayName("Validation DTO Unit Tests")
class ValidationDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Nested
    @DisplayName("CreateRestaurantRequest Validation")
    class CreateRestaurantRequestValidation {

        @Test
        @DisplayName("Should validate valid restaurant request")
        void shouldValidateValidRestaurantRequest() {
            // Given
            CreateRestaurantRequest request = new CreateRestaurantRequest(
                    "Le Petit Bistro",
                    "123 Rue de la Paix, Paris",
                    "+33 1 42 86 87 88",
                    "contact@petitbistro.fr",
                    50,
                    LocalTime.of(11, 0),
                    LocalTime.of(23, 0)
            );

            // When
            Set<ConstraintViolation<CreateRestaurantRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Should reject invalid restaurant name")
        void shouldRejectInvalidRestaurantName(String invalidName) {
            // Given
            CreateRestaurantRequest request = new CreateRestaurantRequest(
                    invalidName,
                    "123 Rue de la Paix, Paris",
                    "+33 1 42 86 87 88",
                    "contact@petitbistro.fr",
                    50,
                    LocalTime.of(11, 0),
                    LocalTime.of(23, 0)
            );

            // When
            Set<ConstraintViolation<CreateRestaurantRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("name");
            assertThat(violations.iterator().next().getMessage()).contains("Restaurant name cannot be blank");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Should reject invalid restaurant address")
        void shouldRejectInvalidRestaurantAddress(String invalidAddress) {
            // Given
            CreateRestaurantRequest request = new CreateRestaurantRequest(
                    "Le Petit Bistro",
                    invalidAddress,
                    "+33 1 42 86 87 88",
                    "contact@petitbistro.fr",
                    50,
                    LocalTime.of(11, 0),
                    LocalTime.of(23, 0)
            );

            // When
            Set<ConstraintViolation<CreateRestaurantRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("address");
        }

        @ParameterizedTest
        @ValueSource(strings = {"invalid-email", "test@", "@domain.com", "test.domain.com"})
        @DisplayName("Should reject invalid email format")
        void shouldRejectInvalidEmailFormat(String invalidEmail) {
            // Given
            CreateRestaurantRequest request = new CreateRestaurantRequest(
                    "Le Petit Bistro",
                    "123 Rue de la Paix, Paris",
                    "+33 1 42 86 87 88",
                    invalidEmail,
                    50,
                    LocalTime.of(11, 0),
                    LocalTime.of(23, 0)
            );

            // When
            Set<ConstraintViolation<CreateRestaurantRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("email");
            assertThat(violations.iterator().next().getMessage()).contains("must be a well-formed email address");
        }

        @Test
        @DisplayName("Should accept null email")
        void shouldAcceptNullEmail() {
            // Given
            CreateRestaurantRequest request = new CreateRestaurantRequest(
                    "Le Petit Bistro",
                    "123 Rue de la Paix, Paris",
                    "+33 1 42 86 87 88",
                    null, // null email should be allowed
                    50,
                    LocalTime.of(11, 0),
                    LocalTime.of(23, 0)
            );

            // When
            Set<ConstraintViolation<CreateRestaurantRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -10})
        @DisplayName("Should reject invalid capacity")
        void shouldRejectInvalidCapacity(int invalidCapacity) {
            // Given
            CreateRestaurantRequest request = new CreateRestaurantRequest(
                    "Le Petit Bistro",
                    "123 Rue de la Paix, Paris",
                    "+33 1 42 86 87 88",
                    "contact@petitbistro.fr",
                    invalidCapacity,
                    LocalTime.of(11, 0),
                    LocalTime.of(23, 0)
            );

            // When
            Set<ConstraintViolation<CreateRestaurantRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("capacity");
            assertThat(violations.iterator().next().getMessage()).contains("must be greater than 0");
        }

        @Test
        @DisplayName("Should reject null opening time")
        void shouldRejectNullOpeningTime() {
            // Given
            CreateRestaurantRequest request = new CreateRestaurantRequest(
                    "Le Petit Bistro",
                    "123 Rue de la Paix, Paris",
                    "+33 1 42 86 87 88",
                    "contact@petitbistro.fr",
                    50,
                    null, // null opening time
                    LocalTime.of(23, 0)
            );

            // When
            Set<ConstraintViolation<CreateRestaurantRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("openingTime");
            assertThat(violations.iterator().next().getMessage()).contains("must not be null");
        }

        @Test
        @DisplayName("Should reject null closing time")
        void shouldRejectNullClosingTime() {
            // Given
            CreateRestaurantRequest request = new CreateRestaurantRequest(
                    "Le Petit Bistro",
                    "123 Rue de la Paix, Paris",
                    "+33 1 42 86 87 88",
                    "contact@petitbistro.fr",
                    50,
                    LocalTime.of(11, 0),
                    null // null closing time
            );

            // When
            Set<ConstraintViolation<CreateRestaurantRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("closingTime");
            assertThat(violations.iterator().next().getMessage()).contains("must not be null");
        }
    }

    @Nested
    @DisplayName("CreateTableRequest Validation")
    class CreateTableRequestValidation {

        @Test
        @DisplayName("Should validate valid table request")
        void shouldValidateValidTableRequest() {
            // Given
            CreateTableRequest request = new CreateTableRequest(
                    1L,
                    4,
                    TableLocation.WINDOW
            );

            // When
            Set<ConstraintViolation<CreateTableRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should reject null restaurant ID")
        void shouldRejectNullRestaurantId() {
            // Given
            CreateTableRequest request = new CreateTableRequest(
                    null, // null restaurant ID
                    4,
                    TableLocation.WINDOW
            );

            // When
            Set<ConstraintViolation<CreateTableRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("restaurantId");
            assertThat(violations.iterator().next().getMessage()).contains("must not be null");
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -5})
        @DisplayName("Should reject invalid seat count")
        void shouldRejectInvalidSeatCount(int invalidSeats) {
            // Given
            CreateTableRequest request = new CreateTableRequest(
                    1L,
                    invalidSeats,
                    TableLocation.WINDOW
            );

            // When
            Set<ConstraintViolation<CreateTableRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("seats");
            assertThat(violations.iterator().next().getMessage()).contains("must be greater than or equal to 1");
        }

        @ParameterizedTest
        @ValueSource(ints = {9, 10, 15})
        @DisplayName("Should reject seats exceeding maximum")
        void shouldRejectSeatsExceedingMaximum(int tooManySeats) {
            // Given
            CreateTableRequest request = new CreateTableRequest(
                    1L,
                    tooManySeats,
                    TableLocation.WINDOW
            );

            // When
            Set<ConstraintViolation<CreateTableRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("seats");
            assertThat(violations.iterator().next().getMessage()).contains("must be less than or equal to 8");
        }

        @Test
        @DisplayName("Should reject null table location")
        void shouldRejectNullTableLocation() {
            // Given
            CreateTableRequest request = new CreateTableRequest(
                    1L,
                    4,
                    null // null location
            );

            // When
            Set<ConstraintViolation<CreateTableRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("location");
            assertThat(violations.iterator().next().getMessage()).contains("must not be null");
        }
    }

    @Nested
    @DisplayName("CreateReservationRequest Validation")
    class CreateReservationRequestValidation {

        @Test
        @DisplayName("Should validate valid reservation request")
        void shouldValidateValidReservationRequest() {
            // Given
            CreateReservationRequest request = new CreateReservationRequest(
                    1L,
                    LocalDate.now().plusDays(1),
                    LocalTime.of(19, 0),
                    LocalTime.of(21, 0),
                    4
            );

            // When
            Set<ConstraintViolation<CreateReservationRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should reject null table ID")
        void shouldRejectNullTableId() {
            // Given
            CreateReservationRequest request = new CreateReservationRequest(
                    null, // null table ID
                    LocalDate.now().plusDays(1),
                    LocalTime.of(19, 0),
                    LocalTime.of(21, 0),
                    4
            );

            // When
            Set<ConstraintViolation<CreateReservationRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("tableId");
        }

        @Test
        @DisplayName("Should reject null date")
        void shouldRejectNullDate() {
            // Given
            CreateReservationRequest request = new CreateReservationRequest(
                    1L,
                    null, // null date
                    LocalTime.of(19, 0),
                    LocalTime.of(21, 0),
                    4
            );

            // When
            Set<ConstraintViolation<CreateReservationRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("date");
        }

        @Test
        @DisplayName("Should reject date in the past")
        void shouldRejectDateInThePast() {
            // Given
            CreateReservationRequest request = new CreateReservationRequest(
                    1L,
                    LocalDate.of(2020, 1, 1), // past date
                    LocalTime.of(19, 0),
                    LocalTime.of(21, 0),
                    4
            );

            // When
            Set<ConstraintViolation<CreateReservationRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("date");
            assertThat(violations.iterator().next().getMessage()).contains("must be a future date");
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -5})
        @DisplayName("Should reject invalid party size")
        void shouldRejectInvalidPartySize(int invalidPartySize) {
            // Given
            CreateReservationRequest request = new CreateReservationRequest(
                    1L,
                    LocalDate.now().plusDays(1),
                    LocalTime.of(19, 0),
                    LocalTime.of(21, 0),
                    invalidPartySize
            );

            // When
            Set<ConstraintViolation<CreateReservationRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("partySize");
            assertThat(violations.iterator().next().getMessage()).contains("must be greater than 0");
        }
    }

    @Nested
    @DisplayName("AvailabilityQuery Validation")
    class AvailabilityQueryValidation {

        @Test
        @DisplayName("Should validate valid availability query")
        void shouldValidateValidAvailabilityQuery() {
            // Given
            AvailabilityQueryRequest request = new AvailabilityQueryRequest(
                    1L,
                    LocalDate.now().plusDays(1),
                    LocalTime.of(19, 0),
                    LocalTime.of(21, 0),
                    4
            );

            // When
            Set<ConstraintViolation<AvailabilityQueryRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should reject query with end time before start time")
        void shouldRejectQueryWithEndTimeBeforeStartTime() {
            // Given
            AvailabilityQueryRequest request = new AvailabilityQueryRequest(
                    1L,
                    LocalDate.now().plusDays(1),
                    LocalTime.of(21, 0), // start time after end time
                    LocalTime.of(19, 0), // end time
                    4
            );

            // When
            Set<ConstraintViolation<AvailabilityQueryRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("End time must be after start time");
        }

        @Test
        @DisplayName("Should reject query with same start and end time")
        void shouldRejectQueryWithSameStartAndEndTime() {
            // Given
            LocalTime sameTime = LocalTime.of(19, 0);
            AvailabilityQueryRequest request = new AvailabilityQueryRequest(
                    1L,
                    LocalDate.now().plusDays(1),
                    sameTime, // same time
                    sameTime, // same time
                    4
            );

            // When
            Set<ConstraintViolation<AvailabilityQueryRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("End time must be after start time");
        }
    }

    // Test DTOs with validation annotations
    record CreateRestaurantRequest(
            @NotBlank(message = "Restaurant name cannot be blank") String name,
            @NotBlank(message = "Restaurant address cannot be blank") String address,
            String phoneNumber,
            @Email(message = "Email must be valid") String email,
            @Positive(message = "Capacity must be positive") Integer capacity,
            @NotNull(message = "Opening time is required") LocalTime openingTime,
            @NotNull(message = "Closing time is required") LocalTime closingTime
    ) {}

    record CreateTableRequest(
            @NotNull(message = "Restaurant ID is required") Long restaurantId,
            @Min(value = 1, message = "Seats must be at least 1")
            @Max(value = 8, message = "Seats cannot exceed 8") Integer seats,
            @NotNull(message = "Table location is required") TableLocation location
    ) {}

    record CreateReservationRequest(
            @NotNull(message = "Table ID is required") Long tableId,
            @NotNull(message = "Date is required")
            @Future(message = "Date must be in the future") LocalDate date,
            @NotNull(message = "Start time is required") LocalTime startTime,
            @NotNull(message = "End time is required") LocalTime endTime,
            @Positive(message = "Party size must be positive") Integer partySize
    ) {}

    @TimeRangeValid(message = "End time must be after start time")
    record AvailabilityQueryRequest(
            @NotNull Long restaurantId,
            @NotNull @Future LocalDate date,
            @NotNull LocalTime startTime,
            @NotNull LocalTime endTime,
            @Positive Integer partySize
    ) {}

    // Mock enum for testing
    enum TableLocation {
        WINDOW, TERRACE, INDOOR, PRIVATE_ROOM
    }

    // Custom validation annotation for testing
    @interface TimeRangeValid {
        String message() default "Invalid time range";
        Class<?>[] groups() default {};
        Class<?>[] payload() default {};
    }
}