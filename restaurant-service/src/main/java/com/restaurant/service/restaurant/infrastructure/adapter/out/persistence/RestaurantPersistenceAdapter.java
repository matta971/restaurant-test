package com.restaurant.service.restaurant.infrastructure.adapter.out.persistence;

import com.restaurant.service.restaurant.domain.model.Restaurant;
import com.restaurant.service.restaurant.domain.model.RestaurantTable;
import com.restaurant.service.restaurant.domain.model.TimeSlot;
import com.restaurant.service.restaurant.domain.port.out.RestaurantRepositoryPort;
import com.restaurant.service.restaurant.infrastructure.adapter.out.persistence.entity.RestaurantEntity;
import com.restaurant.service.restaurant.infrastructure.adapter.out.persistence.entity.RestaurantTableEntity;
import com.restaurant.service.restaurant.infrastructure.adapter.out.persistence.entity.TimeSlotEntity;
import com.restaurant.service.restaurant.infrastructure.adapter.out.persistence.mapper.RestaurantMapper;
import com.restaurant.service.restaurant.infrastructure.adapter.out.persistence.mapper.RestaurantTableMapper;
import com.restaurant.service.restaurant.infrastructure.adapter.out.persistence.mapper.TimeSlotMapper;
import com.restaurant.service.restaurant.infrastructure.adapter.out.persistence.repository.RestaurantJpaRepository;
import com.restaurant.service.restaurant.infrastructure.adapter.out.persistence.repository.RestaurantTableJpaRepository;
import com.restaurant.service.restaurant.infrastructure.adapter.out.persistence.repository.TimeSlotJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JPA implementation of RestaurantRepositoryPort
 * Handles persistence operations for restaurants, tables, and time slots
 */
@Component
@Slf4j
@RequiredArgsConstructor
@Transactional
public class RestaurantPersistenceAdapter implements RestaurantRepositoryPort {

    private final RestaurantJpaRepository restaurantRepository;
    private final RestaurantTableJpaRepository tableRepository;
    private final TimeSlotJpaRepository timeSlotRepository;

    private final RestaurantMapper restaurantMapper;
    private final RestaurantTableMapper tableMapper;
    private final TimeSlotMapper timeSlotMapper;

    // ============ Restaurant Operations (Required by RestaurantRepositoryPort) ============

    @Override
    public Restaurant save(Restaurant restaurant) {
        log.debug("Saving restaurant: {}", restaurant.getName());

        if (restaurant.getId() == null) {
            // Create new restaurant
            RestaurantEntity entity = restaurantMapper.toEntity(restaurant);
            RestaurantEntity savedEntity = restaurantRepository.save(entity);
            return restaurantMapper.toDomain(savedEntity);
        } else {
            // Update existing restaurant
            return restaurantRepository.findById(restaurant.getId())
                    .map(existingEntity -> {
                        restaurantMapper.updateEntity(existingEntity, restaurant);
                        RestaurantEntity updatedEntity = restaurantRepository.save(existingEntity);
                        return restaurantMapper.toDomain(updatedEntity);
                    })
                    .orElseThrow(() -> new IllegalArgumentException("Restaurant not found with id: " + restaurant.getId()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Restaurant> findById(Long id) {
        log.debug("Finding restaurant by id: {}", id);
        return restaurantRepository.findById(id)
                .map(restaurantMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Restaurant> findAll() {
        log.debug("Finding all restaurants");
        return restaurantRepository.findAll().stream()
                .map(restaurantMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Restaurant> findAllActive() {
        log.debug("Finding all active restaurants");
        return restaurantRepository.findAllActive().stream()
                .map(restaurantMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Restaurant> findByNameContaining(String name) {
        log.debug("Finding restaurants by name containing: {}", name);
        return restaurantRepository.findByNameContaining(name).stream()
                .map(restaurantMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Restaurant> findByCity(String city) {
        log.debug("Finding restaurants by city: {}", city);
        return restaurantRepository.findByCity(city).stream()
                .map(restaurantMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        log.debug("Checking if restaurant exists by id: {}", id);
        return restaurantRepository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        log.debug("Deleting restaurant by id: {}", id);
        restaurantRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return restaurantRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countActive() {
        return restaurantRepository.countActive();
    }

    // ============ Extended Operations (Used by Use Cases) ============

    @Transactional(readOnly = true)
    public Optional<Restaurant> findByIdWithTables(Long id) {
        log.debug("Finding restaurant with tables by id: {}", id);
        return restaurantRepository.findByIdWithTables(id)
                .map(restaurantMapper::toDomain);
    }

    @Transactional(readOnly = true)
    public Optional<Restaurant> findByEmail(String email) {
        log.debug("Finding restaurant by email: {}", email);
        return restaurantRepository.findByEmail(email)
                .map(restaurantMapper::toDomain);
    }

    @Transactional(readOnly = true)
    public Page<Restaurant> findAll(Pageable pageable) {
        log.debug("Finding all restaurants with pagination: {}", pageable);
        Page<RestaurantEntity> entityPage = restaurantRepository.findAll(pageable);
        return entityPage.map(restaurantMapper::toDomain);
    }

    @Transactional(readOnly = true)
    public List<Restaurant> findActiveRestaurants() {
        log.debug("Finding active restaurants");
        return restaurantRepository.findAllActive().stream()
                .map(restaurantMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Restaurant> searchByName(String name) {
        log.debug("Searching restaurants by name: {}", name);
        return restaurantRepository.findByNameContaining(name).stream()
                .map(restaurantMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Restaurant> findByNameContainingIgnoreCase(String name) {
        log.debug("Finding restaurants by name containing (ignore case): {}", name);
        return restaurantRepository.findByNameContainingIgnoreCase(name).stream()
                .map(restaurantMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean existsByEmailAndIdNot(String email, Long id) {
        log.debug("Checking if restaurant exists by email {} excluding id {}", email, id);
        return restaurantRepository.existsByEmailAndIdNot(email, id);
    }

    @Transactional(readOnly = true)
    public long countActiveRestaurants() {
        return restaurantRepository.countByActiveTrue();
    }

    // ============ Table Operations (Extended functionality) ============

    public RestaurantTable saveTable(RestaurantTable table) {
        log.debug("Saving table: {} seats at {}", table.getSeats(), table.getLocation());

        if (table.getId() == null) {
            // Create new table
            RestaurantTableEntity entity = tableMapper.toEntity(table);
            RestaurantTableEntity savedEntity = tableRepository.save(entity);
            return tableMapper.toDomain(savedEntity);
        } else {
            // Update existing table
            return tableRepository.findById(table.getId())
                    .map(existingEntity -> {
                        tableMapper.updateEntity(existingEntity, table);
                        RestaurantTableEntity updatedEntity = tableRepository.save(existingEntity);
                        return tableMapper.toDomain(updatedEntity);
                    })
                    .orElseThrow(() -> new IllegalArgumentException("Table not found with id: " + table.getId()));
        }
    }

    @Transactional(readOnly = true)
    public Optional<RestaurantTable> findTableById(Long id) {
        log.debug("Finding table by id: {}", id);
        return tableRepository.findById(id)
                .map(tableMapper::toDomain);
    }

    @Transactional(readOnly = true)
    public Optional<RestaurantTable> findTableByIdWithTimeSlots(Long id) {
        log.debug("Finding table with time slots by id: {}", id);
        return tableRepository.findByIdWithTimeSlots(id)
                .map(tableMapper::toDomain);
    }

    @Transactional(readOnly = true)
    public List<RestaurantTable> findTablesByRestaurantId(Long restaurantId) {
        log.debug("Finding tables by restaurant id: {}", restaurantId);
        return tableRepository.findByRestaurantId(restaurantId).stream()
                .map(tableMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RestaurantTable> findAvailableTablesByRestaurantId(Long restaurantId) {
        log.debug("Finding available tables by restaurant id: {}", restaurantId);
        return tableRepository.findByRestaurantIdAndAvailable(restaurantId, true).stream()
                .map(tableMapper::toDomain)
                .collect(Collectors.toList());
    }

    public void deleteTable(Long tableId) {
        log.debug("Deleting table by id: {}", tableId);
        tableRepository.deleteById(tableId);
    }

    // ============ TimeSlot Operations (Extended functionality) ============

    public TimeSlot saveTimeSlot(TimeSlot timeSlot) {
        log.debug("Saving time slot for date: {}", timeSlot.getDate());

        if (timeSlot.getId() == null) {
            // Create new time slot
            TimeSlotEntity entity = timeSlotMapper.toEntity(timeSlot);
            TimeSlotEntity savedEntity = timeSlotRepository.save(entity);
            return timeSlotMapper.toDomain(savedEntity);
        } else {
            // Update existing time slot
            return timeSlotRepository.findById(timeSlot.getId())
                    .map(existingEntity -> {
                        timeSlotMapper.updateEntity(existingEntity, timeSlot);
                        TimeSlotEntity updatedEntity = timeSlotRepository.save(existingEntity);
                        return timeSlotMapper.toDomain(updatedEntity);
                    })
                    .orElseThrow(() -> new IllegalArgumentException("TimeSlot not found with id: " + timeSlot.getId()));
        }
    }

    @Transactional(readOnly = true)
    public Optional<TimeSlot> findTimeSlotById(Long id) {
        log.debug("Finding time slot by id: {}", id);
        return timeSlotRepository.findById(id)
                .map(timeSlotMapper::toDomain);
    }

    @Transactional(readOnly = true)
    public List<TimeSlot> findTimeSlotsByTableId(Long tableId) {
        log.debug("Finding time slots by table id: {}", tableId);
        return timeSlotRepository.findByTableId(tableId).stream()
                .map(timeSlotMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TimeSlot> findTimeSlotsByDateRange(LocalDate startDate, LocalDate endDate) {
        log.debug("Finding time slots between {} and {}", startDate, endDate);
        return timeSlotRepository.findByDateBetween(startDate, endDate).stream()
                .map(timeSlotMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TimeSlot> findAvailableTimeSlots(Long restaurantId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        log.debug("Finding available time slots for restaurant {} on {} between {} and {}",
                restaurantId, date, startTime, endTime);
        return timeSlotRepository.findAvailableTimeSlots(restaurantId, date, startTime, endTime).stream()
                .map(timeSlotMapper::toDomain)
                .collect(Collectors.toList());
    }

    public void deleteTimeSlot(Long timeSlotId) {
        log.debug("Deleting time slot by id: {}", timeSlotId);
        timeSlotRepository.deleteById(timeSlotId);
    }

    @Transactional(readOnly = true)
    public long countTimeSlotsByStatus(String status) {
        log.debug("Counting time slots by status: {}", status);
        return timeSlotRepository.countByStatus(TimeSlotEntity.TimeSlotStatusEntity.valueOf(status));
    }
}