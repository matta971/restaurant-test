package com.restaurant.service.restaurant.infrastructure.adapter.in.web.exception;

import com.restaurant.service.restaurant.domain.port.in.AvailabilityManagementUseCase;
import com.restaurant.service.restaurant.domain.port.in.RestaurantManagementUseCase;
import com.restaurant.service.restaurant.domain.port.in.TableManagementUseCase;
import com.restaurant.service.restaurant.infrastructure.adapter.in.web.controller.RestaurantController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for GlobalExceptionHandler
 * Testing error handling and response formatting
 */
@WebMvcTest
@ContextConfiguration(classes = {RestaurantController.class, GlobalExceptionHandler.class})
@DisplayName("Global Exception Handler Unit Tests")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RestaurantManagementUseCase restaurantManagementUseCase;

    @MockBean
    private TableManagementUseCase tableManagementUseCase;

    @MockBean
    private AvailabilityManagementUseCase availabilityManagementUseCase;

    @Test
    @DisplayName("Should handle RestaurantNotFoundException with 404 status")
    void shouldHandleRestaurantNotFoundExceptionWith404Status() throws Exception {
        // Given
        given(restaurantManagementUseCase.getRestaurant(999L))
                .willThrow(new RestaurantManagementUseCase.RestaurantNotFoundException(999L));

        // When & Then
        mockMvc.perform(get("/api/restaurants/999"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Restaurant not found with ID: 999"))
                .andExpect(jsonPath("$.path").value("/api/restaurants/999"));
    }

    @Test
    @DisplayName("Should handle TableNotFoundException with 404 status")
    void shouldHandleTableNotFoundExceptionWith404Status() throws Exception {
        // Given
        given(tableManagementUseCase.getTable(999L))
                .willThrow(new TableManagementUseCase.TableNotFoundException(999L));

        // When & Then
        mockMvc.perform(get("/api/restaurants/1/tables/999"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Table not found with ID: 999"));
    }

    @Test
    @DisplayName("Should handle TimeSlotNotFoundException with 404 status")
    void shouldHandleTimeSlotNotFoundExceptionWith404Status() throws Exception {
        // Given
        given(availabilityManagementUseCase.confirmReservation(999L))
                .willThrow(new AvailabilityManagementUseCase.TimeSlotNotFoundException(999L));

        // When & Then
        mockMvc.perform(patch("/api/restaurants/1/reservations/999/confirm"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Time slot not found with ID: 999"));
    }

    @Test
    @DisplayName("Should handle NoTablesAvailableException with 409 status")
    void shouldHandleNoTablesAvailableExceptionWith409Status() throws Exception {
        // Given
        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime startTime = LocalTime.of(19, 0);
        LocalTime endTime = LocalTime.of(21, 0);
        
        given(availabilityManagementUseCase.createReservation(any()))
                .willThrow(new AvailabilityManagementUseCase.NoTablesAvailableException(1L, date, startTime, endTime));

        CreateReservationRequest request = new CreateReservationRequest(
                1L, date, startTime, endTime, 4);

        // When & Then
        mockMvc.perform(post("/api/restaurants/1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("No tables available"));
    }

    @Test
    @DisplayName("Should handle InvalidReservationStateException with 409 status")
    void shouldHandleInvalidReservationStateExceptionWith409Status() throws Exception {
        // Given
        given(availabilityManagementUseCase.confirmReservation(1L))
                .willThrow(new AvailabilityManagementUseCase.InvalidReservationStateException(
                        "Cannot confirm time slot that is not available"));

        // When & Then
        mockMvc.perform(patch("/api/restaurants/1/reservations/1/confirm"))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Cannot confirm time slot that is not available"));
    }

    @Test
    @DisplayName("Should handle validation errors with 400 status")
    void shouldHandleValidationErrorsWith400Status() throws Exception {
        // Given - Create request with invalid data
        CreateRestaurantRequest invalidRequest = new CreateRestaurantRequest(
                "", // blank name
                "", // blank address
                "+33 1 42 86 87 88",
                "invalid-email", // invalid email
                -5, // negative capacity
                LocalTime.of(23, 0),
                LocalTime.of(11, 0) // closing before opening
        );

        // When & Then
        mockMvc.perform(post("/api/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.violations").isArray())
                .andExpect(jsonPath("$.violations").isNotEmpty())
                // Check that validation errors are present
                .andExpect(jsonPath("$.violations[?(@.field == 'name')]").exists())
                .andExpect(jsonPath("$.violations[?(@.field == 'address')]").exists())
                .andExpect(jsonPath("$.violations[?(@.field == 'email')]").exists())
                .andExpect(jsonPath("$.violations[?(@.field == 'capacity')]").exists());
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException with 400 status")
    void shouldHandleIllegalArgumentExceptionWith400Status() throws Exception {
        // Given
        given(restaurantManagementUseCase.createRestaurant(any()))
                .willThrow(new IllegalArgumentException("Restaurant name cannot be null or empty"));

        CreateRestaurantRequest request = new CreateRestaurantRequest(
                "Valid Name", "Valid Address", "+33123456789", "valid@email.com", 50,
                LocalTime.of(11, 0), LocalTime.of(23, 0));

        // When & Then
        mockMvc.perform(post("/api/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Restaurant name cannot be null or empty"));
    }

    @Test
    @DisplayName("Should handle generic Exception with 500 status")
    void shouldHandleGenericExceptionWith500Status() throws Exception {
        // Given
        given(restaurantManagementUseCase.getRestaurant(1L))
                .willThrow(new RuntimeException("Unexpected database error"));

        // When & Then
        mockMvc.perform(get("/api/restaurants/1"))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.path").value("/api/restaurants/1"));
    }

    @Test
    @DisplayName("Should handle invalid JSON with 400 status")
    void shouldHandleInvalidJsonWith400Status() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("JSON parse error"));
    }

    @Test
    @DisplayName("Should handle missing request body with 400 status")
    void shouldHandleMissingRequestBodyWith400Status() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/restaurants")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Required request body is missing"));
    }

    @Test
    @DisplayName("Should handle method not allowed with 405 status")
    void shouldHandleMethodNotAllowedWith405Status() throws Exception {
        // When & Then - POST to a GET-only endpoint
        mockMvc.perform(post("/api/restaurants/1"))
                .andDo(print())
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.error").value("Method Not Allowed"));
    }

    @Test
    @DisplayName("Should handle unsupported media type with 415 status")
    void shouldHandleUnsupportedMediaTypeWith415Status() throws Exception {
        // When & Then - Send XML instead of JSON
        mockMvc.perform(post("/api/restaurants")
                        .contentType(MediaType.APPLICATION_XML)
                        .content("<restaurant></restaurant>"))
                .andDo(print())
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415))
                .andExpect(jsonPath("$.error").value("Unsupported Media Type"));
    }

    // Test DTOs
    record CreateRestaurantRequest(
            String name,
            String address,
            String phoneNumber,
            String email,
            Integer capacity,
            LocalTime openingTime,
            LocalTime closingTime
    ) {}

    record CreateReservationRequest(
            Long tableId,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            Integer partySize
    ) {}
}