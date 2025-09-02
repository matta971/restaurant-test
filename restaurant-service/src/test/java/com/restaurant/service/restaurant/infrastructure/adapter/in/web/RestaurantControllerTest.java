package com.restaurant.service.restaurant.infrastructure.adapter.in.web;

import com.restaurant.service.restaurant.domain.model.Restaurant;
import com.restaurant.service.restaurant.domain.port.in.RestaurantManagementUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.service.restaurant.infrastructure.adapter.in.web.controller.RestaurantController;
import com.restaurant.service.restaurant.infrastructure.adapter.in.web.exception.GlobalExceptionHandler;
import com.restaurant.service.restaurant.infrastructure.adapter.in.web.mapper.RestaurantWebMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for RestaurantController
 * Testing REST API endpoints with mocked use cases
 */
@WebMvcTest(RestaurantController.class)
@Import({GlobalExceptionHandler.class, RestaurantWebMapper.class})
@TestPropertySource(properties = {
        "spring.cloud.loadbalancer.enabled=false",
        "spring.jpa.hibernate.ddl-auto=none"
})
@DisplayName("Restaurant Controller Unit Tests")
class RestaurantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RestaurantManagementUseCase restaurantManagementUseCase;

    @MockBean
    private RestaurantWebMapper mapper;

    private Restaurant testRestaurant;
    private CreateRestaurantRequest createRequest;
    private UpdateRestaurantRequest updateRequest;

    @BeforeEach
    void setUp() {
        testRestaurant = new Restaurant(
                "Le Petit Bistro",
                "123 Rue de la Paix, Paris",
                "+33 1 42 86 87 88",
                "contact@petitbistro.fr",
                50
        );
        testRestaurant.setId(1L);

        createRequest = new CreateRestaurantRequest(
                "Le Petit Bistro",
                "123 Rue de la Paix, Paris",
                "+33 1 42 86 87 88",
                "contact@petitbistro.fr",
                50,
                LocalTime.of(11, 0),
                LocalTime.of(23, 0)
        );

        updateRequest = new UpdateRestaurantRequest(
                "Updated Bistro",
                "456 Updated Street, Paris",
                "+33 1 99 99 99 99",
                "updated@petitbistro.fr",
                75,
                LocalTime.of(10, 0),
                LocalTime.of(23, 59)
        );
    }

    @Nested
    @DisplayName("Create Restaurant")
    class CreateRestaurant {

        @Test
        @DisplayName("Should create restaurant with valid data")
        void shouldCreateRestaurantWithValidData() throws Exception {
            // Given
            given(restaurantManagementUseCase.createRestaurant(any(RestaurantManagementUseCase.CreateRestaurantCommand.class)))
                    .willReturn(testRestaurant);

            // When & Then
            mockMvc.perform(post("/api/restaurants")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Le Petit Bistro"))
                    .andExpect(jsonPath("$.address").value("123 Rue de la Paix, Paris"))
                    .andExpect(jsonPath("$.email").value("contact@petitbistro.fr"))
                    .andExpect(jsonPath("$.capacity").value(50))
                    .andExpect(jsonPath("$.active").value(true))
                    .andExpect(header().string("Location", "/api/restaurants/1"))
                    // HATEOAS links
                    .andExpect(jsonPath("$._links.self.href").exists())
                    .andExpect(jsonPath("$._links.update.href").exists())
                    .andExpect(jsonPath("$._links.delete.href").exists())
                    .andExpect(jsonPath("$._links.tables.href").exists());

            verify(restaurantManagementUseCase).createRestaurant(any(RestaurantManagementUseCase.CreateRestaurantCommand.class));
        }

        @Test
        @DisplayName("Should return 400 when name is blank")
        void shouldReturn400WhenNameIsBlank() throws Exception {
            // Given
            createRequest = new CreateRestaurantRequest(
                    "", // blank name
                    "123 Rue de la Paix, Paris",
                    "+33 1 42 86 87 88",
                    "contact@petitbistro.fr",
                    50,
                    LocalTime.of(11, 0),
                    LocalTime.of(23, 0)
            );

            // When & Then
            mockMvc.perform(post("/api/restaurants")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.violations").isArray())
                    .andExpect(jsonPath("$.violations[0].field").value("name"))
                    .andExpect(jsonPath("$.violations[0].message").value("Restaurant name cannot be blank"));

            verifyNoInteractions(restaurantManagementUseCase);
        }

        @Test
        @DisplayName("Should return 400 when email format is invalid")
        void shouldReturn400WhenEmailFormatIsInvalid() throws Exception {
            // Given
            createRequest = new CreateRestaurantRequest(
                    "Le Petit Bistro",
                    "123 Rue de la Paix, Paris",
                    "+33 1 42 86 87 88",
                    "invalid-email", // invalid email
                    50,
                    LocalTime.of(11, 0),
                    LocalTime.of(23, 0)
            );

            // When & Then
            mockMvc.perform(post("/api/restaurants")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.violations[?(@.field == 'email')]").exists());

            verifyNoInteractions(restaurantManagementUseCase);
        }

        @Test
        @DisplayName("Should return 400 when capacity is negative")
        void shouldReturn400WhenCapacityIsNegative() throws Exception {
            // Given
            createRequest = new CreateRestaurantRequest(
                    "Le Petit Bistro",
                    "123 Rue de la Paix, Paris",
                    "+33 1 42 86 87 88",
                    "contact@petitbistro.fr",
                    -10, // negative capacity
                    LocalTime.of(11, 0),
                    LocalTime.of(23, 0)
            );

            // When & Then
            mockMvc.perform(post("/api/restaurants")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.violations[?(@.field == 'capacity')]").exists());

            verifyNoInteractions(restaurantManagementUseCase);
        }

        @Test
        @DisplayName("Should return 400 when closing time is before opening time")
        void shouldReturn400WhenClosingTimeIsBeforeOpeningTime() throws Exception {
            // Given
            createRequest = new CreateRestaurantRequest(
                    "Le Petit Bistro",
                    "123 Rue de la Paix, Paris",
                    "+33 1 42 86 87 88",
                    "contact@petitbistro.fr",
                    50,
                    LocalTime.of(23, 0), // opening time after closing time
                    LocalTime.of(11, 0)  // closing time
            );

            // When & Then
            mockMvc.perform(post("/api/restaurants")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(restaurantManagementUseCase);
        }
    }

    @Nested
    @DisplayName("Get Restaurant")
    class GetRestaurant {

        @Test
        @DisplayName("Should get restaurant by ID")
        void shouldGetRestaurantById() throws Exception {
            // Given
            given(restaurantManagementUseCase.getRestaurant(1L))
                    .willReturn(testRestaurant);

            // When & Then
            mockMvc.perform(get("/api/restaurants/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Le Petit Bistro"))
                    .andExpect(jsonPath("$.address").value("123 Rue de la Paix, Paris"))
                    // HATEOAS links
                    .andExpect(jsonPath("$._links.self.href").exists())
                    .andExpect(jsonPath("$._links.update.href").exists())
                    .andExpect(jsonPath("$._links.delete.href").exists())
                    .andExpect(jsonPath("$._links.tables.href").exists());

            verify(restaurantManagementUseCase).getRestaurant(1L);
        }

        @Test
        @DisplayName("Should return 404 when restaurant not found")
        void shouldReturn404WhenRestaurantNotFound() throws Exception {
            // Given
            given(restaurantManagementUseCase.getRestaurant(999L))
                    .willThrow(new RestaurantManagementUseCase.RestaurantNotFoundException(999L));

            // When & Then
            mockMvc.perform(get("/api/restaurants/999"))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Restaurant not found with ID: 999"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/api/restaurants/999"));

            verify(restaurantManagementUseCase).getRestaurant(999L);
        }

        @Test
        @DisplayName("Should return 400 for invalid ID format")
        void shouldReturn400ForInvalidIdFormat() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/restaurants/invalid-id"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(restaurantManagementUseCase);
        }
    }

    @Nested
    @DisplayName("Update Restaurant")
    class UpdateRestaurant {

        @Test
        @DisplayName("Should update restaurant with valid data")
        void shouldUpdateRestaurantWithValidData() throws Exception {
            // Given
            Restaurant updatedRestaurant = new Restaurant(
                    "Updated Bistro",
                    "456 Updated Street, Paris",
                    "+33 1 99 99 99 99",
                    "updated@petitbistro.fr",
                    75
            );
            updatedRestaurant.setId(1L);

            given(restaurantManagementUseCase.updateRestaurant(any(RestaurantManagementUseCase.UpdateRestaurantCommand.class)))
                    .willReturn(updatedRestaurant);

            // When & Then
            mockMvc.perform(put("/api/restaurants/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Updated Bistro"))
                    .andExpect(jsonPath("$.address").value("456 Updated Street, Paris"))
                    .andExpect(jsonPath("$.email").value("updated@petitbistro.fr"))
                    .andExpect(jsonPath("$.capacity").value(75));

            verify(restaurantManagementUseCase).updateRestaurant(any(RestaurantManagementUseCase.UpdateRestaurantCommand.class));
        }

        @Test
        @DisplayName("Should return 404 when updating non-existent restaurant")
        void shouldReturn404WhenUpdatingNonExistentRestaurant() throws Exception {
            // Given
            given(restaurantManagementUseCase.updateRestaurant(any(RestaurantManagementUseCase.UpdateRestaurantCommand.class)))
                    .willThrow(new RestaurantManagementUseCase.RestaurantNotFoundException(999L));

            // When & Then
            mockMvc.perform(put("/api/restaurants/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(restaurantManagementUseCase).updateRestaurant(any(RestaurantManagementUseCase.UpdateRestaurantCommand.class));
        }
    }

    @Nested
    @DisplayName("Delete Restaurant")
    class DeleteRestaurant {

        @Test
        @DisplayName("Should delete restaurant")
        void shouldDeleteRestaurant() throws Exception {
            // Given
            willDoNothing().given(restaurantManagementUseCase).deleteRestaurant(1L);

            // When & Then
            mockMvc.perform(delete("/api/restaurants/1"))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            verify(restaurantManagementUseCase).deleteRestaurant(1L);
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent restaurant")
        void shouldReturn404WhenDeletingNonExistentRestaurant() throws Exception {
            // Given
            willThrow(new RestaurantManagementUseCase.RestaurantNotFoundException(999L))
                    .given(restaurantManagementUseCase).deleteRestaurant(999L);

            // When & Then
            mockMvc.perform(delete("/api/restaurants/999"))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(restaurantManagementUseCase).deleteRestaurant(999L);
        }
    }

    @Nested
    @DisplayName("List Restaurants")
    class ListRestaurants {

        @Test
        @DisplayName("Should get all restaurants with pagination")
        void shouldGetAllRestaurantsWithPagination() throws Exception {
            // Given
            Restaurant restaurant2 = new Restaurant(
                    "Big Bistro", "Big Address", "+33222222222", "big@test.com", 100);
            restaurant2.setId(2L);
            
            List<Restaurant> restaurants = List.of(testRestaurant, restaurant2);
            
            given(restaurantManagementUseCase.getAllRestaurants(any()))
                    .willReturn(new PageImpl<>(restaurants, PageRequest.of(0, 10), 2));

            // When & Then
            mockMvc.perform(get("/api/restaurants")
                            .param("page", "0")
                            .param("size", "10")
                            .param("sort", "name,asc"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.content[0].name").value("Le Petit Bistro"))
                    .andExpect(jsonPath("$.content[1].name").value("Big Bistro"))
                    // Pagination metadata
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.size").value(10))
                    .andExpect(jsonPath("$.number").value(0))
                    // HATEOAS links
                    .andExpect(jsonPath("$._links.self.href").exists());

            verify(restaurantManagementUseCase).getAllRestaurants(any());
        }

        @Test
        @DisplayName("Should search restaurants by name")
        void shouldSearchRestaurantsByName() throws Exception {
            // Given
            List<Restaurant> searchResults = List.of(testRestaurant);
            
            given(restaurantManagementUseCase.searchRestaurants(eq("Bistro"),  any()))
                    .willReturn(new PageImpl<>(searchResults, PageRequest.of(0, 10), 1));

            // When & Then
            mockMvc.perform(get("/api/restaurants")
                            .param("name", "Bistro")
                            .param("page", "0")
                            .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].name").value("Le Petit Bistro"));

            verify(restaurantManagementUseCase).searchRestaurants(eq("Bistro"),  any());
        }

        @Test
        @DisplayName("Should search restaurants by city")
        void shouldSearchRestaurantsByCity() throws Exception {
            // Given
            List<Restaurant> searchResults = List.of(testRestaurant);
            
            given(restaurantManagementUseCase.searchRestaurants(eq("Paris"), any()))
                    .willReturn(new PageImpl<>(searchResults, PageRequest.of(0, 10), 1));

            // When & Then
            mockMvc.perform(get("/api/restaurants")
                            .param("city", "Paris")
                            .param("page", "0")
                            .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].address").value("123 Rue de la Paix, Paris"));

            verify(restaurantManagementUseCase).searchRestaurants( eq("Paris"), any());
        }
    }

    @Nested
    @DisplayName("Restaurant Status Operations")
    class RestaurantStatusOperations {

        @Test
        @DisplayName("Should activate restaurant")
        void shouldActivateRestaurant() throws Exception {
            // Given
            testRestaurant.activate();
            given(restaurantManagementUseCase.activateRestaurant(1L))
                    .willReturn(testRestaurant);

            // When & Then
            mockMvc.perform(patch("/api/restaurants/1/activate"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.active").value(true));

            verify(restaurantManagementUseCase).activateRestaurant(1L);
        }

        @Test
        @DisplayName("Should deactivate restaurant")
        void shouldDeactivateRestaurant() throws Exception {
            // Given
            testRestaurant.deactivate();
            given(restaurantManagementUseCase.deactivateRestaurant(1L))
                    .willReturn(testRestaurant);

            // When & Then
            mockMvc.perform(patch("/api/restaurants/1/deactivate"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.active").value(false));

            verify(restaurantManagementUseCase).deactivateRestaurant(1L);
        }
    }

    // Request DTOs for testing
    record CreateRestaurantRequest(
            String name,
            String address,
            String phoneNumber,
            String email,
            Integer capacity,
            LocalTime openingTime,
            LocalTime closingTime
    ) {}

    record UpdateRestaurantRequest(
            String name,
            String address,
            String phoneNumber,
            String email,
            Integer capacity,
            LocalTime openingTime,
            LocalTime closingTime
    ) {}
}