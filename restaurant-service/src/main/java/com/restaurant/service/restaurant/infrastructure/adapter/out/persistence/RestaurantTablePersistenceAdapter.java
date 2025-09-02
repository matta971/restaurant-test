package com.restaurant.service.restaurant.infrastructure.adapter.out.persistence;

import com.restaurant.service.restaurant.domain.model.RestaurantTable;
import com.restaurant.service.restaurant.domain.model.TableLocation;
import com.restaurant.service.restaurant.domain.port.out.RestaurantTableRepositoryPort;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Concrete adapter that implements RestaurantTableRepositoryPort
 * This adapter delegates to the Spring Data JPA repository
 */
@Component
public class RestaurantTablePersistenceAdapter implements RestaurantTableRepositoryPort {

    private final RestaurantTableJpaAdapter restaurantTableJpaRepository;

    public RestaurantTablePersistenceAdapter(RestaurantTableJpaAdapter restaurantTableJpaRepository) {
        this.restaurantTableJpaRepository = restaurantTableJpaRepository;
    }

    @Override
    public RestaurantTable save(RestaurantTable table) {
        return restaurantTableJpaRepository.save(table);
    }

    @Override
    public Optional<RestaurantTable> findById(Long id) {
        return restaurantTableJpaRepository.findById(id);
    }

    @Override
    public List<RestaurantTable> findByRestaurantId(Long restaurantId) {
        return restaurantTableJpaRepository.findByRestaurantId(restaurantId);
    }

    @Override
    public List<RestaurantTable> findAvailableByRestaurantId(Long restaurantId) {
        return restaurantTableJpaRepository.findAvailableByRestaurantId(restaurantId);
    }

    @Override
    public List<RestaurantTable> findByRestaurantIdAndLocation(Long restaurantId, TableLocation location) {
        return restaurantTableJpaRepository.findByRestaurantIdAndLocation(restaurantId, location);
    }

    @Override
    public List<RestaurantTable> findByRestaurantIdAndSeats(Long restaurantId, Integer seats) {
        return restaurantTableJpaRepository.findByRestaurantIdAndSeats(restaurantId, seats);
    }

    @Override
    public List<RestaurantTable> findByRestaurantIdAndSeatsGreaterThanEqual(Long restaurantId, Integer partySize) {
        return restaurantTableJpaRepository.findByRestaurantIdAndSeatsGreaterThanEqual(restaurantId, partySize);
    }

    @Override
    public List<RestaurantTable> findAvailableTablesForDateTimeAndPartySize(Long restaurantId, LocalDate date, 
                                                                           LocalTime startTime, LocalTime endTime, 
                                                                           Integer partySize) {
        return restaurantTableJpaRepository.findAvailableTablesForDateTimeAndPartySize(
            restaurantId, date, startTime, endTime, partySize);
    }

    @Override
    public Optional<RestaurantTable> findByRestaurantIdAndTableNumber(Long restaurantId, String tableNumber) {
        return restaurantTableJpaRepository.findByRestaurantIdAndTableNumber(restaurantId, tableNumber);
    }

    @Override
    public boolean existsById(Long id) {
        return restaurantTableJpaRepository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        restaurantTableJpaRepository.deleteById(id);
    }

    @Override
    public long countByRestaurantId(Long restaurantId) {
        return restaurantTableJpaRepository.countByRestaurantId(restaurantId);
    }

    @Override
    public long countAvailableByRestaurantId(Long restaurantId) {
        return restaurantTableJpaRepository.countAvailableByRestaurantId(restaurantId);
    }
}