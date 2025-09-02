package com.restaurant.service.restaurant.infrastructure.adapter.in.web;

import com.restaurant.service.restaurant.domain.model.RestaurantTable;
import com.restaurant.service.restaurant.domain.model.TableLocation;
import com.restaurant.service.restaurant.domain.port.in.TableManagementUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.service.restaurant.infrastructure.adapter.in.web.controller.TableController;
import com.restaurant.service.restaurant.infrastructure.adapter.in.web.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for TableController
 * Testing REST API endpoints with mocked use cases
 */
@WebMvcTest(controllers = TableController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration.class,
                // ⬇️ Spring Cloud LoadBalancer
                org.springframework.cloud.loadbalancer.config.LoadBalancerAutoConfiguration.class,
                org.springframework.cloud.loadbalancer.config.BlockingLoadBalancerClientAutoConfiguration.class
        })
@DisplayName("Table Controller Unit Tests")
@Import({GlobalExceptionHandler.class})
class TableControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TableManagementUseCase tableManagementUseCase;

    private RestaurantTable testTable;
    private CreateTableRequest createRequest;
    private UpdateTableRequest updateRequest;

    @BeforeEach
    void setUp() {
        testTable = new RestaurantTable(4, TableLocation.WINDOW);
        testTable.setId(1L);
        testTable.setTableNumber("T-12345678");

        createRequest = new CreateTableRequest(1L, 4, TableLocation.WINDOW);
        updateRequest = new UpdateTableRequest(4, TableLocation.TERRACE);
    }

    @Nested
    @DisplayName("Create Table")
    class CreateTable {

        @Test
        @DisplayName("Should create table with valid data")
        void shouldCreateTableWithValidData() throws Exception {
            // Given
            given(tableManagementUseCase.createTable(any(TableManagementUseCase.CreateTableCommand.class)))
                    .willReturn(testTable);

            // When & Then
            mockMvc.perform(post("/api/restaurants/1/tables")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.seats").value(4))
                    .andExpect(jsonPath("$.location").value("WINDOW"))
                    .andExpect(jsonPath("$.available").value(true))
                    .andExpect(jsonPath("$.tableNumber").exists())
                    .andExpect(header().string("Location", "/api/restaurants/1/tables/1"))
                    // HATEOAS links
                    .andExpect(jsonPath("$._links.self.href").exists())
                    .andExpect(jsonPath("$._links.update.href").exists())
                    .andExpect(jsonPath("$._links.delete.href").exists())
                    .andExpect(jsonPath("$._links.restaurant.href").exists());

            verify(tableManagementUseCase).createTable(any(TableManagementUseCase.CreateTableCommand.class));
        }

        @Test
        @DisplayName("Should return 400 when seats is invalid")
        void shouldReturn400WhenSeatsIsInvalid() throws Exception {
            // Given
            createRequest = new CreateTableRequest(1L, 0, TableLocation.WINDOW); // invalid seats

            // When & Then
            mockMvc.perform(post("/api/restaurants/1/tables")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.violations[?(@.field == 'seats')]").exists());

            verifyNoInteractions(tableManagementUseCase);
        }

        @Test
        @DisplayName("Should return 400 when location is null")
        void shouldReturn400WhenLocationIsNull() throws Exception {
            // Given
            createRequest = new CreateTableRequest(1L, 4, null); // null location

            // When & Then
            mockMvc.perform(post("/api/restaurants/1/tables")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.violations[?(@.field == 'location')]").exists());

            verifyNoInteractions(tableManagementUseCase);
        }

        @Test
        @DisplayName("Should return 400 when seats exceeds maximum")
        void shouldReturn400WhenSeatsExceedsMaximum() throws Exception {
            // Given
            createRequest = new CreateTableRequest(1L, 10, TableLocation.WINDOW); // exceeds max of 8

            // When & Then
            mockMvc.perform(post("/api/restaurants/1/tables")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(tableManagementUseCase);
        }
    }

    @Nested
    @DisplayName("Get Table")
    class GetTable {

        @Test
        @DisplayName("Should get table by ID")
        void shouldGetTableById() throws Exception {
            // Given
            given(tableManagementUseCase.getTable(1L))
                    .willReturn(testTable);

            // When & Then
            mockMvc.perform(get("/api/restaurants/1/tables/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.seats").value(4))
                    .andExpect(jsonPath("$.location").value("WINDOW"))
                    .andExpect(jsonPath("$.available").value(true))
                    // HATEOAS links
                    .andExpect(jsonPath("$._links.self.href").exists())
                    .andExpect(jsonPath("$._links.update.href").exists());

            verify(tableManagementUseCase).getTable(1L);
        }

        @Test
        @DisplayName("Should return 404 when table not found")
        void shouldReturn404WhenTableNotFound() throws Exception {
            // Given
            given(tableManagementUseCase.getTable(999L))
                    .willThrow(new TableManagementUseCase.TableNotFoundException(999L));

            // When & Then
            mockMvc.perform(get("/api/restaurants/1/tables/999"))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Table not found with ID: 999"));

            verify(tableManagementUseCase).getTable(999L);
        }
    }

    @Nested
    @DisplayName("Update Table")
    class UpdateTable {

        @Test
        @DisplayName("Should update table with valid data")
        void shouldUpdateTableWithValidData() throws Exception {
            // Given
            RestaurantTable updatedTable = new RestaurantTable(4, TableLocation.TERRACE);
            updatedTable.setId(1L);
            updatedTable.setTableNumber("T-12345678");

            given(tableManagementUseCase.updateTable(any(TableManagementUseCase.UpdateTableCommand.class)))
                    .willReturn(updatedTable);

            // When & Then
            mockMvc.perform(put("/api/restaurants/1/tables/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.seats").value(4))
                    .andExpect(jsonPath("$.location").value("TERRACE"));

            verify(tableManagementUseCase).updateTable(any(TableManagementUseCase.UpdateTableCommand.class));
        }

        @Test
        @DisplayName("Should return 404 when updating non-existent table")
        void shouldReturn404WhenUpdatingNonExistentTable() throws Exception {
            // Given
            given(tableManagementUseCase.updateTable(any(TableManagementUseCase.UpdateTableCommand.class)))
                    .willThrow(new TableManagementUseCase.TableNotFoundException(999L));

            // When & Then
            mockMvc.perform(put("/api/restaurants/1/tables/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(tableManagementUseCase).updateTable(any(TableManagementUseCase.UpdateTableCommand.class));
        }
    }

    @Nested
    @DisplayName("Delete Table")
    class DeleteTable {

        @Test
        @DisplayName("Should delete table")
        void shouldDeleteTable() throws Exception {
            // Given
            willDoNothing().given(tableManagementUseCase).deleteTable(1L);

            // When & Then
            mockMvc.perform(delete("/api/restaurants/1/tables/1"))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            verify(tableManagementUseCase).deleteTable(1L);
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent table")
        void shouldReturn404WhenDeletingNonExistentTable() throws Exception {
            // Given
            willThrow(new TableManagementUseCase.TableNotFoundException(999L))
                    .given(tableManagementUseCase).deleteTable(999L);

            // When & Then
            mockMvc.perform(delete("/api/restaurants/1/tables/999"))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(tableManagementUseCase).deleteTable(999L);
        }
    }

    @Nested
    @DisplayName("List Tables")
    class ListTables {

        @Test
        @DisplayName("Should get all tables for restaurant")
        void shouldGetAllTablesForRestaurant() throws Exception {
            // Given
            RestaurantTable table2 = new RestaurantTable(6, TableLocation.TERRACE);
            table2.setId(2L);
            table2.setTableNumber("T-87654321");

            List<RestaurantTable> tables = List.of(testTable, table2);
            
            given(tableManagementUseCase.getRestaurantTables(1L))
                    .willReturn(tables);

            // When & Then
            mockMvc.perform(get("/api/restaurants/1/tables"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.content[0].seats").value(4))
                    .andExpect(jsonPath("$.content[1].seats").value(6))
                    // HATEOAS links
                    .andExpect(jsonPath("$._links.self.href").exists());

            verify(tableManagementUseCase).getRestaurantTables(1L);
        }

        @Test
        @DisplayName("Should filter tables by location")
        void shouldFilterTablesByLocation() throws Exception {
            // Given
            List<RestaurantTable> windowTables = List.of(testTable);
            
            given(tableManagementUseCase.getTablesByLocation(1L, TableLocation.WINDOW))
                    .willReturn(windowTables);

            // When & Then
            mockMvc.perform(get("/api/restaurants/1/tables")
                            .param("location", "WINDOW"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].location").value("WINDOW"));

            verify(tableManagementUseCase).getTablesByLocation(1L, TableLocation.WINDOW);
        }

        @Test
        @DisplayName("Should filter tables by party size")
        void shouldFilterTablesByPartySize() throws Exception {
            // Given
            List<RestaurantTable> suitableTables = List.of(testTable);
            
            given(tableManagementUseCase.getTablesByPartySize(1L, 4))
                    .willReturn(suitableTables);

            // When & Then
            mockMvc.perform(get("/api/restaurants/1/tables")
                            .param("partySize", "4"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].seats").value(4));

            verify(tableManagementUseCase).getTablesByPartySize(1L, 4);
        }

        @Test
        @DisplayName("Should get available tables only")
        void shouldGetAvailableTablesOnly() throws Exception {
            // Given
            List<RestaurantTable> availableTables = List.of(testTable);
            
            given(tableManagementUseCase.getAvailableTables(1L))
                    .willReturn(availableTables);

            // When & Then
            mockMvc.perform(get("/api/restaurants/1/tables")
                            .param("available", "true"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].available").value(true));

            verify(tableManagementUseCase).getAvailableTables(1L);
        }
    }

    @Nested
    @DisplayName("Table Availability Operations")
    class TableAvailabilityOperations {

        @Test
        @DisplayName("Should make table available")
        void shouldMakeTableAvailable() throws Exception {
            // Given
            testTable.makeAvailable();
            given(tableManagementUseCase.makeTableAvailable(1L))
                    .willReturn(testTable);

            // When & Then
            mockMvc.perform(patch("/api/restaurants/1/tables/1/available"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.available").value(true));

            verify(tableManagementUseCase).makeTableAvailable(1L);
        }

        @Test
        @DisplayName("Should make table unavailable")
        void shouldMakeTableUnavailable() throws Exception {
            // Given
            testTable.makeUnavailable();
            given(tableManagementUseCase.makeTableUnavailable(1L))
                    .willReturn(testTable);

            // When & Then
            mockMvc.perform(patch("/api/restaurants/1/tables/1/unavailable"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.available").value(false));

            verify(tableManagementUseCase).makeTableUnavailable(1L);
        }
    }

    @Nested
    @DisplayName("Table Statistics")
    class TableStatistics {

        @Test
        @DisplayName("Should get table utilization statistics")
        void shouldGetTableUtilizationStatistics() throws Exception {
            // Given
            TableManagementUseCase.TableUtilizationStats stats = 
                    new TableManagementUseCase.TableUtilizationStats(
                            1L, 5, 4, 1, 20, 0.8);
            
            given(tableManagementUseCase.getTableUtilization(1L))
                    .willReturn(stats);

            // When & Then
            mockMvc.perform(get("/api/restaurants/1/tables/statistics"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.restaurantId").value(1))
                    .andExpect(jsonPath("$.totalTables").value(5))
                    .andExpect(jsonPath("$.availableTables").value(4))
                    .andExpect(jsonPath("$.unavailableTables").value(1))
                    .andExpect(jsonPath("$.totalSeats").value(20))
                    .andExpect(jsonPath("$.availabilityRate").value(0.8));

            verify(tableManagementUseCase).getTableUtilization(1L);
        }
    }

    // Request DTOs for testing
    record CreateTableRequest(
            Long restaurantId,
            Integer seats,
            TableLocation location
    ) {}

    record UpdateTableRequest(
            Integer seats,
            TableLocation location
    ) {}
}