package com.restaurant.service.restaurant.infrastructure.adapter.out.persistence;

import com.restaurant.service.restaurant.domain.model.TimeSlot;
import com.restaurant.service.restaurant.domain.model.TimeSlotStatus;
import com.restaurant.service.restaurant.domain.port.out.TimeSlotRepositoryPort;
import com.restaurant.service.restaurant.infrastructure.adapter.out.persistence.entity.TimeSlotEntity;
import com.restaurant.service.restaurant.infrastructure.adapter.out.persistence.mapper.TimeSlotMapper;
import com.restaurant.service.restaurant.infrastructure.adapter.out.persistence.repository.TimeSlotJpaRepository;
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
 * Concrete adapter that implements TimeSlotRepositoryPort
 * This adapter delegates to the Spring Data JPA repository
 */
@Component
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TimeSlotPersistenceAdapter implements TimeSlotRepositoryPort {

    private final TimeSlotJpaRepository timeSlotJpaRepository;
    private final TimeSlotMapper timeSlotMapper;

    @Override
    public TimeSlot save(TimeSlot timeSlot) {
        log.debug("Saving time slot for date: {}", timeSlot.getDate());
        var entity = timeSlotMapper.toEntity(timeSlot);
        var savedEntity = timeSlotJpaRepository.save(entity);
        return timeSlotMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<TimeSlot> findById(Long id) {
        log.debug("Finding time slot by id: {}", id);
        return timeSlotJpaRepository.findById(id)
                .map(timeSlotMapper::toDomain);
    }

    @Override
    public List<TimeSlot> findByTableId(Long tableId) {
        log.debug("Finding time slots by table id: {}", tableId);
        return timeSlotJpaRepository.findByTableIdAndDate(tableId, LocalDate.now()).stream()
                .map(timeSlotMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<TimeSlot> findByTableIdAndDate(Long tableId, LocalDate date) {
        log.debug("Finding time slots by table id {} and date {}", tableId, date);
        return timeSlotJpaRepository.findByTableIdAndDate(tableId, date).stream()
                .map(timeSlotMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<TimeSlot> findByTableIdAndStatus(Long tableId, TimeSlotStatus status) {
        log.debug("Finding time slots by table id {} and status {}", tableId, status);
        var entityStatus = timeSlotMapper.mapStatusToEntity(status);
        return timeSlotJpaRepository.findByTableIdAndDateAndStatus(tableId, LocalDate.now(), entityStatus).stream()
                .map(timeSlotMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<TimeSlot> findOverlappingTimeSlots(Long tableId, LocalDate date,
                                                   LocalTime startTime, LocalTime endTime) {
        log.debug("Finding overlapping time slots for table {} on {} between {} and {}",
                tableId, date, startTime, endTime);
        return timeSlotJpaRepository.findConflictingTimeSlots(tableId, date, startTime, endTime).stream()
                .map(timeSlotMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<TimeSlot> findActiveTimeSlotsByTableIdAndDateRange(Long tableId, LocalDate startDate,
                                                                   LocalDate endDate) {
        return timeSlotJpaRepository.findByTableIdAndDate(tableId, startDate).stream()
                .filter(entity -> !entity.getDate().isBefore(startDate) && !entity.getDate().isAfter(endDate))
                .filter(entity -> entity.getStatus() != TimeSlotEntity.TimeSlotStatusEntity.CANCELLED)
                .map(timeSlotMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<TimeSlot> findByRestaurantIdAndDate(Long restaurantId, LocalDate date) {
        log.debug("Finding time slots by restaurant id {} and date {}", restaurantId, date);
        return timeSlotJpaRepository.findByRestaurantAndDate(restaurantId, date).stream()
                .map(timeSlotMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeSlot> findByRestaurantIdAndStatus(Long restaurantId, TimeSlotStatus status) {
        var entityStatus = timeSlotMapper.mapStatusToEntity(status);
        return timeSlotJpaRepository.findByRestaurantAndDate(restaurantId, LocalDate.now()).stream()
                .filter(entity -> entity.getStatus() == entityStatus)
                .map(timeSlotMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<TimeSlot> findByRestaurantIdAndDateBetween(Long restaurantId, LocalDate startDate, LocalDate endDate) {
        return List.of();
    }

    @Override
    public List<TimeSlot> findAvailableByRestaurantIdAndDate(Long restaurantId, LocalDate date) {
        return List.of();
    }

    @Override
    public List<TimeSlot> findConflictingTimeSlots(Long tableId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        return List.of();
    }

    @Override
    public List<TimeSlot> findUpcomingReservations(Long restaurantId, int limit) {
        return List.of();
    }

    @Override
    public List<TimeSlot> findReservationsInTimeRange(Long restaurantId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeSlot> findUpcomingByRestaurantId(Long restaurantId, LocalDate fromDate) {
        log.debug("Finding upcoming time slots by restaurant id {} from date {}", restaurantId, fromDate);
        return timeSlotJpaRepository.findUpcomingReservations(restaurantId, fromDate).stream()
                .map(timeSlotMapper::toDomain)
                .collect(Collectors.toList());
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
        var timeSlots = timeSlotJpaRepository.findByTableIdAndDate(tableId, LocalDate.now());
        timeSlotJpaRepository.deleteAll(timeSlots);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByTableId(Long tableId) {
        return timeSlotJpaRepository.findByTableIdAndDate(tableId, LocalDate.now()).size();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByRestaurantIdAndDate(Long restaurantId, LocalDate date) {
        log.debug("Counting time slots by restaurant id {} and date {}", restaurantId, date);
        return timeSlotJpaRepository.findByRestaurantAndDate(restaurantId, date).size();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByRestaurantIdAndStatus(Long restaurantId, TimeSlotStatus status) {
        log.debug("Counting time slots by restaurant id {} and status {}", restaurantId, status);
        var entityStatus = timeSlotMapper.mapStatusToEntity(status);
        return timeSlotJpaRepository.countByRestaurantAndStatus(restaurantId, entityStatus);
    }

    @Override
    public List<TimeSlot> findExpiredTimeSlots(LocalDate cutoffDate) {
        return timeSlotJpaRepository.findExpiredTimeSlots(cutoffDate).stream()
                .map(timeSlotMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public UtilizationStats getUtilizationStats(Long restaurantId, LocalDate date) {
        // Implémentation simple calculant les stats à partir des données
        List<TimeSlot> allSlots = findByRestaurantIdAndDate(restaurantId, date);

        long totalSlots = allSlots.size();
        long reservedSlots = allSlots.stream().mapToLong(slot ->
                slot.getStatus() == TimeSlotStatus.RESERVED ? 1 : 0).sum();
        long availableSlots = allSlots.stream().mapToLong(slot ->
                slot.getStatus() == TimeSlotStatus.AVAILABLE ? 1 : 0).sum();
        long confirmedSlots = allSlots.stream().mapToLong(slot ->
                slot.getStatus() == TimeSlotStatus.CONFIRMED ? 1 : 0).sum();
        long cancelledSlots = allSlots.stream().mapToLong(slot ->
                slot.getStatus() == TimeSlotStatus.CANCELLED ? 1 : 0).sum();

        double utilizationRate = totalSlots > 0 ? (double) (reservedSlots + confirmedSlots) / totalSlots : 0.0;

        return new UtilizationStats(totalSlots, reservedSlots, availableSlots,
                confirmedSlots, cancelledSlots, utilizationRate);
    }
}