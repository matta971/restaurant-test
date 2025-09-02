package com.restaurant.service.restaurant.infrastructure.adapter.in.web.util;

import com.restaurant.service.restaurant.domain.model.Restaurant;
import com.restaurant.service.restaurant.domain.model.RestaurantTable;
import com.restaurant.service.restaurant.domain.model.TableLocation;
import com.restaurant.service.restaurant.domain.model.TimeSlot;
import com.restaurant.service.restaurant.infrastructure.adapter.in.web.controller.RestaurantController;
import com.restaurant.service.restaurant.infrastructure.adapter.in.web.controller.TableController;
import com.restaurant.service.restaurant.infrastructure.adapter.in.web.controller.AvailabilityController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * Unit tests for HATEOAS utilities and response formatting
 * Testing link generation and JSON serialization
 */
@DisplayName("HATEOAS Utils Unit Tests")
class HateoasUtilsTest {

    private ObjectMapper objectMapper;
    private Restaurant testRestaurant;
    private RestaurantTable testTable;
    private TimeSlot testTimeSlot;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        testRestaurant = new Restaurant(
                "Le Petit Bistro",
                "123 Rue de la Paix, Paris",
                "+33 1 42 86 87 88",
                "contact@petitbistro.fr",
                50
        );
        testRestaurant.setId(1L);

        testTable = new RestaurantTable(4, TableLocation.WINDOW);
        testTable.setId(1L);
        testTable.setTableNumber("T-12345678");

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        testTimeSlot = new TimeSlot(tomorrow, LocalTime.of(19, 0), LocalTime.of(21, 0), 4);
        testTimeSlot.setId(1L);
        testTimeSlot.setTable(testTable);
    }

    @Nested
    @DisplayName("Restaurant HATEOAS Links")
    class RestaurantHateoasLinks {

        @Test
        @DisplayName("Should create restaurant entity model with all required links")
        void shouldCreateRestaurantEntityModelWithAllRequiredLinks() throws Exception {
            // Given & When
            EntityModel<Restaurant> entityModel = createRestaurantEntityModel(testRestaurant);

            // Then
            assertThat(entityModel.getContent()).isEqualTo(testRestaurant);
            
            // Check required links
            assertThat(entityModel.hasLink("self")).isTrue();
            assertThat(entityModel.hasLink("update")).isTrue();
            assertThat(entityModel.hasLink("delete")).isTrue();
            assertThat(entityModel.hasLink("tables")).isTrue();
            assertThat(entityModel.hasLink("availability")).isTrue();
            
            // Verify link URIs
            Link selfLink = entityModel.getRequiredLink("self");
            assertThat(selfLink.getHref()).contains("/api/restaurants/1");
            
            Link tablesLink = entityModel.getRequiredLink("tables");
            assertThat(tablesLink.getHref()).contains("/api/restaurants/1/tables");
            
            Link availabilityLink = entityModel.getRequiredLink("availability");
            assertThat(availabilityLink.getHref()).contains("/api/restaurants/1/availability");
        }

        @Test
        @DisplayName("Should serialize restaurant entity model to JSON correctly")
        void shouldSerializeRestaurantEntityModelToJsonCorrectly() throws Exception {
            // Given
            EntityModel<Restaurant> entityModel = createRestaurantEntityModel(testRestaurant);

            // When
            String json = objectMapper.writeValueAsString(entityModel);

            // Then
            assertThat(json).contains("\"id\":1");
            assertThat(json).contains("\"name\":\"Le Petit Bistro\"");
            assertThat(json).contains("\"address\":\"123 Rue de la Paix, Paris\"");
            assertThat(json).contains("\"_links\"");
            assertThat(json).contains("\"self\"");
            assertThat(json).contains("\"update\"");
            assertThat(json).contains("\"delete\"");
            assertThat(json).contains("\"tables\"");
        }

        @Test
        @DisplayName("Should include conditional links based on restaurant state")
        void shouldIncludeConditionalLinksBasedOnRestaurantState() throws Exception {
            // Given - Active restaurant
            EntityModel<Restaurant> activeModel = createRestaurantEntityModel(testRestaurant);

            // When - Deactivate restaurant
            testRestaurant.deactivate();
            EntityModel<Restaurant> inactiveModel = createRestaurantEntityModel(testRestaurant);

            // Then
            assertThat(activeModel.hasLink("deactivate")).isTrue();
            assertThat(activeModel.hasLink("activate")).isFalse();
            
            assertThat(inactiveModel.hasLink("activate")).isTrue();
            assertThat(inactiveModel.hasLink("deactivate")).isFalse();
        }

        private EntityModel<Restaurant> createRestaurantEntityModel(Restaurant restaurant) {
            return EntityModel.of(restaurant)
                    .add(linkTo(methodOn(RestaurantController.class).getRestaurant(restaurant.getId())).withSelfRel())
                    .add(linkTo(methodOn(RestaurantController.class).updateRestaurant(restaurant.getId(), null)).withRel("update"))
                    .add(linkTo(methodOn(RestaurantController.class).deleteRestaurant(restaurant.getId())).withRel("delete"))
                    .add(linkTo(methodOn(TableController.class).getRestaurantTables(restaurant.getId(), null, null, null, null)).withRel("tables"))
                    .add(linkTo(methodOn(AvailabilityController.class).checkAvailability(restaurant.getId(), null, null, null, null)).withRel("availability"))
                    .addIf(restaurant.isActive(), () -> 
                            linkTo(methodOn(RestaurantController.class).deactivateRestaurant(restaurant.getId())).withRel("deactivate"))
                    .addIf(!restaurant.isActive(), () -> 
                            linkTo(methodOn(RestaurantController.class).activateRestaurant(restaurant.getId())).withRel("activate"));
        }
    }

    @Nested
    @DisplayName("Table HATEOAS Links")
    class TableHateoasLinks {

        @Test
        @DisplayName("Should create table entity model with all required links")
        void shouldCreateTableEntityModelWithAllRequiredLinks() throws Exception {
            // Given & When
            EntityModel<RestaurantTable> entityModel = createTableEntityModel(testTable, 1L);

            // Then
            assertThat(entityModel.getContent()).isEqualTo(testTable);
            
            // Check required links
            assertThat(entityModel.hasLink("self")).isTrue();
            assertThat(entityModel.hasLink("update")).isTrue();
            assertThat(entityModel.hasLink("delete")).isTrue();
            assertThat(entityModel.hasLink("restaurant")).isTrue();
            
            // Verify link URIs
            Link selfLink = entityModel.getRequiredLink("self");
            assertThat(selfLink.getHref()).contains("/api/restaurants/1/tables/1");
            
            Link restaurantLink = entityModel.getRequiredLink("restaurant");
            assertThat(restaurantLink.getHref()).contains("/api/restaurants/1");
        }

        @Test
        @DisplayName("Should include availability management links")
        void shouldIncludeAvailabilityManagementLinks() throws Exception {
            // Given - Available table
            EntityModel<RestaurantTable> availableModel = createTableEntityModel(testTable, 1L);

            // When - Make table unavailable
            testTable.makeUnavailable();
            EntityModel<RestaurantTable> unavailableModel = createTableEntityModel(testTable, 1L);

            // Then
            assertThat(availableModel.hasLink("makeUnavailable")).isTrue();
            assertThat(availableModel.hasLink("makeAvailable")).isFalse();
            
            assertThat(unavailableModel.hasLink("makeAvailable")).isTrue();
            assertThat(unavailableModel.hasLink("makeUnavailable")).isFalse();
        }

        private EntityModel<RestaurantTable> createTableEntityModel(RestaurantTable table, Long restaurantId) {
            return EntityModel.of(table)
                    .add(linkTo(methodOn(TableController.class).getTable(restaurantId, table.getId())).withSelfRel())
                    .add(linkTo(methodOn(TableController.class).updateTable(restaurantId, table.getId(), null)).withRel("update"))
                    .add(linkTo(methodOn(TableController.class).deleteTable(restaurantId, table.getId())).withRel("delete"))
                    .add(linkTo(methodOn(RestaurantController.class).getRestaurant(restaurantId)).withRel("restaurant"))
                    .addIf(table.isAvailable(), () -> 
                            linkTo(methodOn(TableController.class).makeTableUnavailable(restaurantId, table.getId())).withRel("makeUnavailable"))
                    .addIf(!table.isAvailable(), () -> 
                            linkTo(methodOn(TableController.class).makeTableAvailable(restaurantId, table.getId())).withRel("makeAvailable"));
        }
    }

    @Nested
    @DisplayName("TimeSlot HATEOAS Links")
    class TimeSlotHateoasLinks {

        @Test
        @DisplayName("Should create time slot entity model with status-based links")
        void shouldCreateTimeSlotEntityModelWithStatusBasedLinks() throws Exception {
            // Given & When - Available time slot
            EntityModel<TimeSlot> availableModel = createTimeSlotEntityModel(testTimeSlot, 1L);

            // Then
            assertThat(availableModel.getContent()).isEqualTo(testTimeSlot);
            assertThat(availableModel.hasLink("self")).isTrue();
            assertThat(availableModel.hasLink("confirm")).isTrue();
            assertThat(availableModel.hasLink("cancel")).isFalse(); // Can't cancel AVAILABLE slot
            assertThat(availableModel.hasLink("complete")).isFalse(); // Can't complete AVAILABLE slot
        }

        @Test
        @DisplayName("Should update links based on time slot status transitions")
        void shouldUpdateLinksBasedOnTimeSlotStatusTransitions() throws Exception {
            // Given - AVAILABLE time slot
            EntityModel<TimeSlot> availableModel = createTimeSlotEntityModel(testTimeSlot, 1L);

            // When - Confirm time slot
            testTimeSlot.confirm();
            EntityModel<TimeSlot> confirmedModel = createTimeSlotEntityModel(testTimeSlot, 1L);

            // When - Complete time slot
            testTimeSlot.complete();
            EntityModel<TimeSlot> completedModel = createTimeSlotEntityModel(testTimeSlot, 1L);

            // Then - Available slot links
            assertThat(availableModel.hasLink("confirm")).isTrue();
            assertThat(availableModel.hasLink("cancel")).isFalse();
            assertThat(availableModel.hasLink("complete")).isFalse();

            // Then - Confirmed slot links
            assertThat(confirmedModel.hasLink("confirm")).isFalse();
            assertThat(confirmedModel.hasLink("cancel")).isTrue();
            assertThat(confirmedModel.hasLink("complete")).isTrue();

            // Then - Completed slot links (no actions allowed)
            assertThat(completedModel.hasLink("confirm")).isFalse();
            assertThat(completedModel.hasLink("cancel")).isFalse();
            assertThat(completedModel.hasLink("complete")).isFalse();
        }

        private EntityModel<TimeSlot> createTimeSlotEntityModel(TimeSlot timeSlot, Long restaurantId) {
            return EntityModel.of(timeSlot)
                    .add(linkTo(methodOn(AvailabilityController.class).getReservation(restaurantId, timeSlot.getId())).withSelfRel())
                    .add(linkTo(methodOn(TableController.class).getTable(restaurantId, timeSlot.getTable().getId())).withRel("table"))
                    .add(linkTo(methodOn(RestaurantController.class).getRestaurant(restaurantId)).withRel("restaurant"))
                    .addIf(timeSlot.getStatus().allowsConfirmation(), () -> 
                            linkTo(methodOn(AvailabilityController.class).confirmReservation(restaurantId, timeSlot.getId())).withRel("confirm"))
                    .addIf(timeSlot.getStatus().allowsCancellation(), () -> 
                            linkTo(methodOn(AvailabilityController.class).cancelReservation(restaurantId, timeSlot.getId())).withRel("cancel"))
                    .addIf(timeSlot.getStatus() == com.restaurant.service.restaurant.domain.model.TimeSlotStatus.CONFIRMED, () -> 
                            linkTo(methodOn(AvailabilityController.class).completeReservation(restaurantId, timeSlot.getId())).withRel("complete"));
        }
    }

    @Nested
    @DisplayName("Collection Response Models")
    class CollectionResponseModels {

        @Test
        @DisplayName("Should create paginated collection with navigation links")
        void shouldCreatePaginatedCollectionWithNavigationLinks() throws Exception {
            // Given
            List<Restaurant> restaurants = List.of(testRestaurant);
            PagedCollectionResponse<Restaurant> response = new PagedCollectionResponse<>(
                    restaurants, 0, 10, 1, 1);

            // When
            String json = objectMapper.writeValueAsString(response);

            // Then
            assertThat(json).contains("\"content\"");
            assertThat(json).contains("\"totalElements\":1");
            assertThat(json).contains("\"totalPages\":1");
            assertThat(json).contains("\"size\":10");
            assertThat(json).contains("\"number\":0");
            assertThat(json).contains("\"first\":true");
            assertThat(json).contains("\"last\":true");
        }

        @Test
        @DisplayName("Should create availability response with query metadata")
        void shouldCreateAvailabilityResponseWithQueryMetadata() throws Exception {
            // Given
            AvailabilityResponse response = new AvailabilityResponse(
                    List.of(testTable),
                    new AvailabilityQuery(1L, LocalDate.now().plusDays(1), 
                            LocalTime.of(19, 0), LocalTime.of(21, 0), 4));

            // When
            String json = objectMapper.writeValueAsString(response);

            // Then
            assertThat(json).contains("\"availableTables\"");
            assertThat(json).contains("\"query\"");
            assertThat(json).contains("\"restaurantId\":1");
            assertThat(json).contains("\"partySize\":4");
        }
    }

    @Nested
    @DisplayName("Error Response Models")
    class ErrorResponseModels {

        @Test
        @DisplayName("Should create standard error response")
        void shouldCreateStandardErrorResponse() throws Exception {
            // Given
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .timestamp(java.time.Instant.now())
                    .status(404)
                    .error("Not Found")
                    .message("Restaurant not found with ID: 999")
                    .path("/api/restaurants/999")
                    .build();

            // When
            String json = objectMapper.writeValueAsString(errorResponse);

            // Then
            assertThat(json).contains("\"timestamp\"");
            assertThat(json).contains("\"status\":404");
            assertThat(json).contains("\"error\":\"Not Found\"");
            assertThat(json).contains("\"message\":\"Restaurant not found with ID: 999\"");
            assertThat(json).contains("\"path\":\"/api/restaurants/999\"");
        }

        @Test
        @DisplayName("Should create validation error response with violations")
        void shouldCreateValidationErrorResponseWithViolations() throws Exception {
            // Given
            List<FieldViolation> violations = List.of(
                    new FieldViolation("name", "Restaurant name cannot be blank"),
                    new FieldViolation("email", "Email must be valid")
            );
            
            ValidationErrorResponse errorResponse = ValidationErrorResponse.builder()
                    .timestamp(java.time.Instant.now())
                    .status(400)
                    .error("Bad Request")
                    .message("Validation failed")
                    .path("/api/restaurants")
                    .violations(violations)
                    .build();

            // When
            String json = objectMapper.writeValueAsString(errorResponse);

            // Then
            assertThat(json).contains("\"violations\"");
            assertThat(json).contains("\"field\":\"name\"");
            assertThat(json).contains("\"message\":\"Restaurant name cannot be blank\"");
            assertThat(json).contains("\"field\":\"email\"");
            assertThat(json).contains("\"message\":\"Email must be valid\"");
        }
    }

    // Response model classes for testing
    record PagedCollectionResponse<T>(
            List<T> content,
            int number,
            int size,
            long totalElements,
            int totalPages
    ) {
        public boolean isFirst() { return number == 0; }
        public boolean isLast() { return number == totalPages - 1; }
    }

    record AvailabilityResponse(
            List<RestaurantTable> availableTables,
            AvailabilityQuery query
    ) {}

    record AvailabilityQuery(
            Long restaurantId,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            Integer partySize
    ) {}

    record ErrorResponse(
            java.time.Instant timestamp,
            int status,
            String error,
            String message,
            String path
    ) {
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private java.time.Instant timestamp;
            private int status;
            private String error;
            private String message;
            private String path;

            public Builder timestamp(java.time.Instant timestamp) {
                this.timestamp = timestamp;
                return this;
            }

            public Builder status(int status) {
                this.status = status;
                return this;
            }

            public Builder error(String error) {
                this.error = error;
                return this;
            }

            public Builder message(String message) {
                this.message = message;
                return this;
            }

            public Builder path(String path) {
                this.path = path;
                return this;
            }

            public ErrorResponse build() {
                return new ErrorResponse(timestamp, status, error, message, path);
            }
        }
    }

    record ValidationErrorResponse(
            java.time.Instant timestamp,
            int status,
            String error,
            String message,
            String path,
            List<FieldViolation> violations
    ) {
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private java.time.Instant timestamp;
            private int status;
            private String error;
            private String message;
            private String path;
            private List<FieldViolation> violations;

            public Builder timestamp(java.time.Instant timestamp) {
                this.timestamp = timestamp;
                return this;
            }

            public Builder status(int status) {
                this.status = status;
                return this;
            }

            public Builder error(String error) {
                this.error = error;
                return this;
            }

            public Builder message(String message) {
                this.message = message;
                return this;
            }

            public Builder path(String path) {
                this.path = path;
                return this;
            }

            public Builder violations(List<FieldViolation> violations) {
                this.violations = violations;
                return this;
            }

            public ValidationErrorResponse build() {
                return new ValidationErrorResponse(timestamp, status, error, message, path, violations);
            }
        }
    }

    record FieldViolation(String field, String message) {}
}