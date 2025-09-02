package com.restaurant.service.restaurant.infrastructure.adapter.out.persistence;

import com.restaurant.service.restaurant.domain.model.RestaurantTable;
import com.restaurant.service.restaurant.domain.model.TableLocation;
import com.restaurant.service.restaurant.domain.port.out.RestaurantTableRepositoryPort;
import com.restaurant.service.restaurant.infrastructure.adapter.out.persistence.mapper.RestaurantTableMapper;
import com.restaurant.service.restaurant.infrastructure.adapter.out.persistence.repository.RestaurantTableJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Concrete adapter that implements RestaurantTableRepositoryPort
 * This adapter delegates to the Spring Data JPA repository
 */
@Component
@Slf4j
@RequiredArgsConstructor
@Transactional
public class RestaurantTablePersistenceAdapter implements RestaurantTableRepositoryPort {

    private final RestaurantTableJpaRepository  restaurantTableJpaRepository;
    private final RestaurantTableMapper restaurantTableMapper;

    @Override
    public RestaurantTable save(RestaurantTable table) {
        log.debug("Saving restaurant table with {} seats", table.getSeats());
        var entity = restaurantTableMapper.toEntity(table);
        var savedEntity = restaurantTableJpaRepository.save(entity);
        return restaurantTableMapper.toDomain(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RestaurantTable> findById(Long id) {
        log.debug("Finding restaurant table by id: {}", id);
        return restaurantTableJpaRepository.findById(id)
                .map(restaurantTableMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantTable> findByRestaurantId(Long restaurantId) {
        log.debug("Finding tables by restaurant id: {}", restaurantId);
        return restaurantTableJpaRepository.findByRestaurantId(restaurantId).stream()
                .map(restaurantTableMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantTable> findAvailableByRestaurantId(Long restaurantId) {
        log.debug("Finding available tables by restaurant id: {}", restaurantId);
        return restaurantTableJpaRepository.findByRestaurantIdAndAvailableTrue(restaurantId).stream()
                .map(restaurantTableMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantTable> findByRestaurantIdAndLocation(Long restaurantId, TableLocation location) {
        log.debug("Finding tables by restaurant id {} and location {}", restaurantId, location);
        var entityLocation = restaurantTableMapper.mapLocationToEntity(location);
        return restaurantTableJpaRepository.findByRestaurantIdAndLocation(restaurantId, entityLocation).stream()
                .map(restaurantTableMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantTable> findByRestaurantIdAndSeats(Long restaurantId, Integer seats) {
        log.debug("Finding tables by restaurant id {} and seats {}", restaurantId, seats);
        // Cette méthode exacte n'existe pas dans le repository, utilisons une alternative
        return restaurantTableJpaRepository.findByRestaurantId(restaurantId).stream()
                .filter(entity -> entity.getSeats().equals(seats))
                .map(restaurantTableMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantTable> findByRestaurantIdAndSeatsGreaterThanEqual(Long restaurantId, Integer partySize) {
        log.debug("Finding tables by restaurant id {} with seats >= {}", restaurantId, partySize);
        return restaurantTableJpaRepository.findByRestaurantIdAndSeatsGreaterThanEqual(restaurantId, partySize).stream()
                .map(restaurantTableMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantTable> findAvailableTablesForDateTimeAndPartySize(Long restaurantId, LocalDate date,
                                                                            LocalTime startTime, LocalTime endTime,
                                                                            Integer partySize) {
        log.debug("Finding available tables for restaurant {} on {} from {} to {} for {} people",
                restaurantId, date, startTime, endTime, partySize);
        // Cette requête complexe n'existe pas encore, implémentation temporaire
        return findByRestaurantIdAndSeatsGreaterThanEqual(restaurantId, partySize).stream()
                .filter(table -> table.getAvailable())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RestaurantTable> findByRestaurantIdAndTableNumber(Long restaurantId, String tableNumber) {
        log.debug("Finding table by restaurant id {} and table number {}", restaurantId, tableNumber);
        try {
            Integer tableNum = Integer.valueOf(tableNumber);
            return restaurantTableJpaRepository.findByRestaurantIdAndTableNumber(restaurantId, tableNum)
                    .map(restaurantTableMapper::toDomain);
        } catch (NumberFormatException e) {
            log.warn("Invalid table number format: {}", tableNumber);
            return Optional.empty();
        }
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
    @Transactional(readOnly = true)
    public long countByRestaurantId(Long restaurantId) {
        return restaurantTableJpaRepository.countByRestaurantId(restaurantId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAvailableByRestaurantId(Long restaurantId) {
        return restaurantTableJpaRepository.countByRestaurantIdAndAvailableTrue(restaurantId);
    }
}