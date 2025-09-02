package com.restaurant.service.restaurant.infrastructure.adapter.in.web;

import com.restaurant.service.restaurant.domain.model.RestaurantTable;
import com.restaurant.service.restaurant.domain.model.TableLocation;
import com.restaurant.service.restaurant.domain.model.TimeSlot;
import com.restaurant.service.restaurant.domain.model.TimeSlotStatus;
import com.restaurant.service.restaurant.domain.port.in.AvailabilityManagementUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.service.restaurant.infrastructure.adapter.in.web.controller.AvailabilityController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AvailabilityController
 * Testing REST API endpoints with mocked use cases
 */
@WebMvcTest(AvailabilityController.class)
@DisplayName("Availability Controller Unit Tests")
class AvailabilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AvailabilityManagementUseCase availabilityManagementUseCase;

    private RestaurantTable testTable;
    private TimeSlot testTimeSlot;
    private CreateReservationRequest createReservationRequest;

    @BeforeEach
    void setUp() {
        testTable = new RestaurantTable(4, TableLocation.WINDOW);
        testTable.setId(1L);
        testTable.setTableNumber("T-12345678");

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        testTimeSlot = new TimeSlot(tomorrow, LocalTime.of(19, 0), LocalTime.of(21, 0), 4);
        testTimeSlot.setId(1L);
        testTimeSlot.setTable(testTable);

        createReservationRequest = new CreateReservationRequest(
                1L, // tableId
                tomorrow,
                LocalTime.of(19, 0),
                LocalTime.of(21, 0),
                4
        );
    }

    @Nested
    @DisplayName("Check Availability")
    class CheckAvailability {

        @Test
        @DisplayName("Should find available tables")
        void shouldFindAvailableTables() throws Exception {
            // Given
            RestaurantTable table2 = new RestaurantTable(6, TableLocation.TERRACE);
            table2.setId(2L);
            
            List<RestaurantTable> availableTables = List.of(testTable, table2);
            
            given(availabilityManagementUseCase.findAvailableTables(any(AvailabilityManagementUseCase.AvailabilityQuery.class)))
                    .willReturn(availableTables);

            // When & Then
            mockMvc.perform(get("/api/restaurants/1/availability")
                            .param("date", "2024-12-26")
                            .param("startTime", "19:00")
                            .param("endTime", "21:00")
                            .param("partySize", "4"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.availableTables").isArray())
                    .andExpect(jsonPath("$.availableTables.length()").value(2))
                    .andExpect(jsonPath("$.availableTables[0].seats").value(4))
                    .andExpect(jsonPath("$.availableTables[1].seats").value(6))
                    .andExpect(jsonPath("$.query.restaurantId").value(1))
                    .andExpect(jsonPath("$.query.date").value("2024-12-26"))
                    .andExpect(jsonPath("$.query.startTime").value("19:00:00"))
                    .andExpect(jsonPath("$.query.endTime").value("21:00:00"))
                    .andExpect(jsonPath("$.query.partySize").value(4))
                    // HATEOAS links
                    .andExpect(jsonPath("$._links.self.href").exists())
                    .andExpect(jsonPath("$._links.restaurant.href").exists());

            verify(availabilityManagementUseCase).findAvailableTables(any(AvailabilityManagementUseCase.AvailabilityQuery.class));
        }

        @Test
        @DisplayName("Should return empty list when no tables available")
        void shouldReturnEmptyListWhenNoTablesAvailable() throws Exception {
            // Given
            given(availabilityManagementUseCase.findAvailableTables(any(AvailabilityManagementUseCase.AvailabilityQuery.class)))
                    .willReturn(List.of());

            // When & Then
            mockMvc.perform(get("/api/restaurants/1/availability")
                            .param("date", "2024-12-26")
                            .param("startTime", "19:00")
                            .param("endTime", "21:00")
                            .param("partySize", "8"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.availableTables").isArray())
                    .andExpect(jsonPath("$.availableTables.length()").value(0));

            verify(availabilityManagementUseCase).findAvailableTables(any(AvailabilityManagementUseCase.AvailabilityQuery.class));
        }

        @Test
        @DisplayName("Should return 400 for invalid date format")
        void shouldReturn400ForInvalidDateFormat() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/restaurants/1/availability")
                            .param("date", "invalid-date")
                            .param("startTime", "19:00")
                            .param("endTime", "21:00")
                            .param("partySize", "4"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(availabilityManagementUseCase);
        }

        @Test
        @DisplayName("Should return 400 for missing required parameters")
        void shouldReturn400ForMissingRequiredParameters() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/restaurants/1/availability"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(availabilityManagementUseCase);
        }

        @Test
        @DisplayName("Should return 400 when end time is before start time")
        void shouldReturn400WhenEndTimeIsBeforeStartTime() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/restaurants/1/availability")
                            .param("date", "2024-12-26")
                            .param("startTime", "21:00")
                            .param("endTime", "19:00") // Before start time
                            .param("partySize", "4"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(availabilityManagementUseCase);
        }
    }

    @Nested
    @DisplayName("Find Best Table")
    class FindBestTable {

        @Test
        @DisplayName("Should find best available table")
        void shouldFindBestAvailableTable() throws Exception {
            // Given
            given(availabilityManagementUseCase.findBestAvailableTable(any(AvailabilityManagementUseCase.AvailabilityQuery.class)))
                    .willReturn(testTable);

            // When & Then
            mockMvc.perform(get("/api/restaurants/1/availability/best")
                            .param("date", "2024-12-26")
                            .param("startTime", "19:00")
                            .param("endTime", "21:00")
                            .param("partySize", "4"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.table.id").value(1))
                    .andExpect(jsonPath("$.table.seats").value(4))
                    .andExpect(jsonPath("$.table.location").value("WINDOW"));

            verify(availabilityManagementUseCase).findBestAvailableTable(any(AvailabilityManagementUseCase.AvailabilityQuery.class));
        }

        @Test
        @DisplayName("Should return 404 when no suitable table found")
        void shouldReturn404WhenNoSuitableTableFound() throws Exception {
            // Given
            given(availabilityManagementUseCase.findBestAvailableTable(any(AvailabilityManagementUseCase.AvailabilityQuery.class)))
                    .willReturn(null);

            // When & Then
            mockMvc.perform(get("/api/restaurants/1/availability/best")
                            .param("date", "2024-12-26")
                            .param("startTime", "19:00")
                            .param("endTime", "21:00")
                            .param("partySize", "10"))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("No suitable table available for the requested criteria"));

            verify(availabilityManagementUseCase).findBestAvailableTable(any(AvailabilityManagementUseCase.AvailabilityQuery.class));
        }
    }

    @Nested
    @DisplayName("Create Reservation")
    class CreateReservation {

        @Test
        @DisplayName("Should create reservation with valid data")
        void shouldCreateReservationWithValidData() throws Exception {
            // Given
            given(availabilityManagementUseCase.createReservation(any(AvailabilityManagementUseCase.CreateReservationCommand.class)))
                    .willReturn(testTimeSlot);

            // When & Then
            mockMvc.perform(post("/api/restaurants/1/reservations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createReservationRequest)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.date").value("2024-12-26"))
                    .andExpect(jsonPath("$.startTime").value("19:00:00"))
                    .andExpect(jsonPath("$.endTime").value("21:00:00"))
                    .andExpect(jsonPath("$.reservedSeats").value(4))
                    .andExpect(jsonPath("$.status").value("AVAILABLE"))
                    .andExpect(header().string("Location", "/api/restaurants/1/reservations/1"))
                    // HATEOAS links
                    .andExpect(jsonPath("$._links.self.href").exists())
                    .andExpect(jsonPath("$._links.confirm.href").exists())
                    .andExpect(jsonPath("$._links.cancel.href").exists());

            verify(availabilityManagementUseCase).createReservation(any(AvailabilityManagementUseCase.CreateReservationCommand.class));
        }

        @Test
        @DisplayName("Should return 400 when table ID is null")
        void shouldReturn400WhenTableIdIsNull() throws Exception {
            // Given
            createReservationRequest = new CreateReservationRequest(
                    null, // null tableId
                    LocalDate.now().plusDays(1),
                    LocalTime.of(19, 0),
                    LocalTime.of(21, 0),
                    4
            );

            // When & Then
            mockMvc.perform(post("/api/restaurants/1/reservations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createReservationRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.violations[?(@.field == 'tableId')]").exists());

            verifyNoInteractions(availabilityManagementUseCase);
        }

        @Test
        @DisplayName("Should return 400 when party size is invalid")
        void shouldReturn400WhenPartySizeIsInvalid() throws Exception {
            // Given
            createReservationRequest = new CreateReservationRequest(
                    1L,
                    LocalDate.now().plusDays(1),
                    LocalTime.of(19, 0),
                    LocalTime.of(21, 0),
                    0 // invalid party size
            );

            // When & Then
            mockMvc.perform(post("/api/restaurants/1/reservations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createReservationRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.violations[?(@.field == 'partySize')]").exists());

            verifyNoInteractions(availabilityManagementUseCase);
        }

        @Test
        @DisplayName("Should return 409 when no tables available")
        void shouldReturn409WhenNoTablesAvailable() throws Exception {
            // Given
            given(availabilityManagementUseCase.createReservation(any(AvailabilityManagementUseCase.CreateReservationCommand.class)))
                    .willThrow(new AvailabilityManagementUseCase.NoTablesAvailableException(1L, 
                            LocalDate.now().plusDays(1), LocalTime.of(19, 0), LocalTime.of(21, 0)));

            // When & Then
            mockMvc.perform(post("/api/restaurants/1/reservations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createReservationRequest)))
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").exists());

            verify(availabilityManagementUseCase).createReservation(any(AvailabilityManagementUseCase.CreateReservationCommand.class));
        }
    }

    @Nested
    @DisplayName("Reservation Status Operations")
    class ReservationStatusOperations {

        @Test
        @DisplayName("Should confirm reservation")
        void shouldConfirmReservation() throws Exception {
            // Given
            testTimeSlot.confirm();
            given(availabilityManagementUseCase.confirmReservation(1L))
                    .willReturn(testTimeSlot);

            // When & Then
            mockMvc.perform(patch("/api/restaurants/1/reservations/1/confirm"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.status").value("CONFIRMED"));

            verify(availabilityManagementUseCase).confirmReservation(1L);
        }

        @Test
        @DisplayName("Should cancel reservation")
        void shouldCancelReservation() throws Exception {
            // Given
            testTimeSlot.confirm();
            testTimeSlot.cancel();
            given(availabilityManagementUseCase.cancelReservation(1L))
                    .willReturn(testTimeSlot);

            // When & Then
            mockMvc.perform(patch("/api/restaurants/1/reservations/1/cancel"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.status").value("CANCELLED"));

            verify(availabilityManagementUseCase).cancelReservation(1L);
        }

        @Test
        @DisplayName("Should complete reservation")
        void shouldCompleteReservation() throws Exception {
            // Given
            testTimeSlot.confirm();
            testTimeSlot.complete();
            given(availabilityManagementUseCase.completeReservation(1L))
                    .willReturn(testTimeSlot);

            // When & Then
            mockMvc.perform(patch("/api/restaurants/1/reservations/1/complete"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.status").value("COMPLETED"));

            verify(availabilityManagementUseCase).completeReservation(1L);
        }

        @Test
        @DisplayName("Should return 404 when reservation not found")
        void shouldReturn404WhenReservationNotFound() throws Exception {
            // Given
            given(availabilityManagementUseCase.confirmReservation(999L))
                    .willThrow(new AvailabilityManagementUseCase.TimeSlotNotFoundException(999L));

            // When & Then
            mockMvc.perform(patch("/api/restaurants/1/reservations/999/confirm"))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Time slot not found with ID: 999"));

            verify(availabilityManagementUseCase).confirmReservation(999L);
        }

        @Test
        @DisplayName("Should return 409 for invalid state transition")
        void shouldReturn409ForInvalidStateTransition() throws Exception {
            // Given
            given(availabilityManagementUseCase.confirmReservation(1L))
                    .willThrow(new AvailabilityManagementUseCase.InvalidReservationStateException(
                            "Cannot confirm time slot that is not available"));

            // When & Then
            mockMvc.perform(patch("/api/restaurants/1/reservations/1/confirm"))
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Cannot confirm time slot that is not available"));

            verify(availabilityManagementUseCase).confirmReservation(1L);
        }
    }

    @Nested
    @DisplayName("Get Reservations")
    class GetReservations {

        @Test
        @DisplayName("Should get reservations for date")
        void shouldGetReservationsForDate() throws Exception {
            // Given
            List<TimeSlot> reservations = List.of(testTimeSlot);
            
            given(availabilityManagementUseCase.getReservationsForDate(1L, LocalDate.of(2024, 12, 26)))
                    .willReturn(reservations);

            // When & Then
            mockMvc.perform(get("/api/restaurants/1/reservations")
                            .param("date", "2024-12-26"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].date").value("2024-12-26"));

            verify(availabilityManagementUseCase).getReservationsForDate(1L, LocalDate.of(2024, 12, 26));
        }

        @Test
        @DisplayName("Should get reservations by status")
        void shouldGetReservationsByStatus() throws Exception {
            // Given
            testTimeSlot.confirm();
            List<TimeSlot> confirmedReservations = List.of(testTimeSlot);
            
            given(availabilityManagementUseCase.getReservationsByStatus(1L, TimeSlotStatus.CONFIRMED))
                    .willReturn(confirmedReservations);

            // When & Then
            mockMvc.perform(get("/api/restaurants/1/reservations")
                            .param("status", "CONFIRMED"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].status").value("CONFIRMED"));

            verify(availabilityManagementUseCase).getReservationsByStatus(1L, TimeSlotStatus.CONFIRMED);
        }

        @Test
        @DisplayName("Should get upcoming reservations")
        void shouldGetUpcomingReservations() throws Exception {
            // Given
            List<TimeSlot> upcomingReservations = List.of(testTimeSlot);
            
            given(availabilityManagementUseCase.getUpcomingReservations(1L))
                    .willReturn(upcomingReservations);

            // When & Then
            mockMvc.perform(get("/api/restaurants/1/reservations/upcoming"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].date").exists());

            verify(availabilityManagementUseCase).getUpcomingReservations(1L);
        }
    }

    @Nested
    @DisplayName("Capacity Statistics")
    class CapacityStatistics {

        @Test
        @DisplayName("Should get capacity statistics")
        void shouldGetCapacityStatistics() throws Exception {
            // Given
            AvailabilityManagementUseCase.CapacityStats stats = 
                    new AvailabilityManagementUseCase.CapacityStats(
                            1L, 20, 16, 5, 4, 0.8);
            
            given(availabilityManagementUseCase.getCapacityStats(1L))
                    .willReturn(stats);

            // When & Then
            mockMvc.perform(get("/api/restaurants/1/capacity"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.restaurantId").value(1))
                    .andExpect(jsonPath("$.totalSeats").value(20))
                    .andExpect(jsonPath("$.availableSeats").value(16))
                    .andExpect(jsonPath("$.totalTables").value(5))
                    .andExpect(jsonPath("$.availableTables").value(4))
                    .andExpect(jsonPath("$.availabilityRate").value(0.8));

            verify(availabilityManagementUseCase).getCapacityStats(1L);
        }

        @Test
        @DisplayName("Should calculate availability rate")
        void shouldCalculateAvailabilityRate() throws Exception {
            // Given
            given(availabilityManagementUseCase.calculateAvailabilityRate(1L, LocalDate.of(2024, 12, 26)))
                    .willReturn(0.75);

            // When & Then
            mockMvc.perform(get("/api/restaurants/1/availability-rate")
                            .param("date", "2024-12-26"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.restaurantId").value(1))
                    .andExpect(jsonPath("$.date").value("2024-12-26"))
                    .andExpect(jsonPath("$.availabilityRate").value(0.75));

            verify(availabilityManagementUseCase).calculateAvailabilityRate(1L, LocalDate.of(2024, 12, 26));
        }

        @Test
        @DisplayName("Should calculate utilization rate")
        void shouldCalculateUtilizationRate() throws Exception {
            // Given
            given(availabilityManagementUseCase.calculateUtilizationRate(1L, 
                    LocalDate.of(2024, 12, 26), LocalTime.of(19, 30)))
                    .willReturn(0.6);

            // When & Then
            mockMvc.perform(get("/api/restaurants/1/utilization-rate")
                            .param("date", "2024-12-26")
                            .param("time", "19:30"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.restaurantId").value(1))
                    .andExpect(jsonPath("$.date").value("2024-12-26"))
                    .andExpect(jsonPath("$.time").value("19:30:00"))
                    .andExpect(jsonPath("$.utilizationRate").value(0.6));

            verify(availabilityManagementUseCase).calculateUtilizationRate(1L, 
                    LocalDate.of(2024, 12, 26), LocalTime.of(19, 30));
        }
    }

    // Request DTOs for testing
    record CreateReservationRequest(
            Long tableId,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            Integer partySize
    ) {}
}