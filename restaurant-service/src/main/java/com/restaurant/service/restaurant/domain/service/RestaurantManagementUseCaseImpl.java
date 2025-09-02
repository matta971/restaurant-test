package com.restaurant.service.restaurant.domain.service;

import com.restaurant.service.restaurant.domain.model.Restaurant;
import com.restaurant.service.restaurant.domain.port.in.RestaurantManagementUseCase;
import com.restaurant.service.restaurant.domain.port.out.RestaurantRepositoryPort;
import com.restaurant.service.restaurant.domain.port.out.EventPublisherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Implementation of Restaurant Management Use Case
 * Handles all business logic related to restaurant operations
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RestaurantManagementUseCaseImpl implements RestaurantManagementUseCase {

    private final RestaurantRepositoryPort restaurantRepository;
    private final EventPublisherPort eventPublisher;

    @Override
    public Restaurant createRestaurant(CreateRestaurantCommand command) {
        var restaurant = new Restaurant(
                command.name(),
                command.address(),
                command.phoneNumber(),
                command.email(),
                command.capacity(),
                command.openingTime(),
                command.closingTime()
        );

        var savedRestaurant = restaurantRepository.save(restaurant);

        // Publish domain event
        var event = new EventPublisherPort.RestaurantCreatedEvent(
                savedRestaurant.getId(),
                savedRestaurant.getName(),
                savedRestaurant.getAddress(),
                savedRestaurant.getCapacity(),
                Instant.now()
        );
        eventPublisher.publishEvent(event);

        return savedRestaurant;
    }

    @Override
    public Restaurant updateRestaurant(UpdateRestaurantCommand command) {
        var restaurant = restaurantRepository.findById(command.id())
                .orElseThrow(() -> new RestaurantNotFoundException(command.id()));

        // Update restaurant properties
        restaurant.setName(command.name());
        restaurant.setAddress(command.address());
        restaurant.setPhoneNumber(command.phoneNumber());
        restaurant.setEmail(command.email());
        restaurant.setCapacity(command.capacity());
        restaurant.setOpeningTime(command.openingTime());
        restaurant.setClosingTime(command.closingTime());

        return restaurantRepository.save(restaurant);
    }

    @Override
    @Transactional(readOnly = true)
    public Restaurant getRestaurant(Long restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Restaurant> getAllRestaurants(Pageable pageable) {
        List<Restaurant> restaurants = restaurantRepository.findAll();

        // Simple pagination implementation - in real app, use repository pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), restaurants.size());
        List<Restaurant> pageContent = restaurants.subList(start, end);

        return new PageImpl<>(pageContent, pageable, restaurants.size());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Restaurant> searchRestaurants(String name, Pageable pageable) {
        List<Restaurant> restaurants;

        if (name == null || name.trim().isEmpty()) {
            restaurants = restaurantRepository.findAll();
        } else {
            restaurants = restaurantRepository.findByNameContaining(name.trim());
        }

        // Simple pagination implementation
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), restaurants.size());
        List<Restaurant> pageContent = restaurants.subList(start, end);

        return new PageImpl<>(pageContent, pageable, restaurants.size());
    }

    @Override
    public Restaurant activateRestaurant(Long restaurantId) {
        var restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));

        restaurant.activate();
        var savedRestaurant = restaurantRepository.save(restaurant);

        // Publish domain event
        var event = new EventPublisherPort.RestaurantStatusChangedEvent(
                savedRestaurant.getId(),
                true,
                Instant.now()
        );
        eventPublisher.publishEvent(event);

        return savedRestaurant;
    }

    @Override
    public Restaurant deactivateRestaurant(Long restaurantId) {
        var restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));

        restaurant.deactivate();
        var savedRestaurant = restaurantRepository.save(restaurant);

        // Publish domain event
        var event = new EventPublisherPort.RestaurantStatusChangedEvent(
                savedRestaurant.getId(),
                false,
                Instant.now()
        );
        eventPublisher.publishEvent(event);

        return savedRestaurant;
    }

    @Override
    public void deleteRestaurant(Long restaurantId) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new RestaurantNotFoundException(restaurantId);
        }

        restaurantRepository.deleteById(restaurantId);
    }

    @Override
    @Transactional(readOnly = true)
    public RestaurantStats getRestaurantStats(Long restaurantId) {
        var restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));

        var tables = restaurant.getTables();
        long totalTables = tables.size();
        long availableTables = tables.stream()
                .mapToLong(table -> table.isAvailable() ? 1 : 0)
                .sum();

        int totalSeats = restaurant.getCapacity();
        int availableSeats = restaurant.getTotalAvailableSeats();

        return new RestaurantStats(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getCapacity(),
                (int) totalTables,
                (int) availableTables,
                totalSeats,
                availableSeats,
                restaurant.isActive()
        );
    }
}