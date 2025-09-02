package com.restaurant.service.restaurant.infrastructure.adapter.out.persistence;

import com.restaurant.service.restaurant.domain.model.TimeSlot;
import com.restaurant.service.restaurant.domain.model.TimeSlotStatus;
import com.restaurant.service.restaurant.domain.port.out.TimeSlotRepositoryPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Concrete adapter that implements TimeSlotRepositoryPort
 * This adapter delegates to the Spring Data JPA repository
 */
@Component
public class TimeSlotPersistenceAdapter implements TimeSlotRepositoryPort {

    private final TimeSlotJpaAdapter timeSlotJpaRepository;

    public TimeSlotPersistenceAdapter(TimeSlotJpaAdapter timeSlotJpaRepository) {
        this.timeSlotJpaRepository = timeSlotJpaRepository;
    }

    @Override
    public TimeSlot save(TimeSlot timeSlot) {
        return timeSlotJpaRepository.save(timeSlot);
    }

    @Override
    public Optional<TimeSlot> findById(Long id) {
        return timeSlotJpaRepository.findById(id);
    }

    @Override
    public List<TimeSlot> findByTableId(Long tableId) {
        return timeSlotJpaRepository.findByTableId(tableId);
    }

    @Override
    public List<TimeSlot> findByTableIdAndDate(Long tableId, LocalDate date) {
        return timeSlotJpaRepository.findByTableIdAndDate(tableId, date);
    }

    @Override
    public List<TimeSlot> findByTableIdAndStatus(Long tableId, TimeSlotStatus status) {
        return timeSlotJpaRepository.findByTableIdAndStatus(tableId, status);
    }

    @Override
    public List<TimeSlot> findOverlappingTimeSlots(Long tableId, LocalDate date, 
                                                  LocalTime startTime, LocalTime endTime) {
        return timeSlotJpaRepository.findOverlappingTimeSlots(tableId, date, startTime, endTime);
    }

    @Override
    public List<TimeSlot> findActiveTimeSlotsByTableIdAndDateRange(Long tableId, LocalDate startDate, 
                                                                  LocalDate endDate) {
        return timeSlotJpaRepository.findActiveTimeSlotsByTableIdAndDateRange(tableId, startDate, endDate);
    }

    @Override
    public List<TimeSlot> findByRestaurantIdAndDate(Long restaurantId, LocalDate date) {
        return timeSlotJpaRepository.findByRestaurantIdAndDate(restaurantId, date);
    }

    @Override
    public List<TimeSlot> findByRestaurantIdAndStatus(Long restaurantId, TimeSlotStatus status) {
        return timeSlotJpaRepository.findByRestaurantIdAndStatus(restaurantId, status);
    }

    @Override
    public List<TimeSlot> findUpcomingByRestaurantId(Long restaurantId, LocalDate fromDate) {
        return timeSlotJpaRepository.findUpcomingByRestaurantId(restaurantId, fromDate);
    }

    @Override
    public boolean existsById(Long id) {
        return timeSlotJpaRepository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        timeSlotJpaRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteByTableId(Long tableId) {
        timeSlotJpaRepository.deleteByTableId(tableId);
    }

    @Override
    public long countByTableId(Long tableId) {
        return timeSlotJpaRepository.countByTableId(tableId);
    }

    @Override
    public long countByRestaurantIdAndStatus(Long restaurantId, TimeSlotStatus status) {
        return timeSlotJpaRepository.countByRestaurantIdAndStatus(restaurantId, status);
    }
}