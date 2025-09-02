package com.restaurant.service.restaurant.infrastructure.adapter.out.persistence.mapper;

import com.restaurant.service.restaurant.domain.model.TimeSlot;
import com.restaurant.service.restaurant.domain.model.TimeSlotStatus;
import com.restaurant.service.restaurant.infrastructure.adapter.out.persistence.entity.TimeSlotEntity;
import com.restaurant.service.restaurant.infrastructure.adapter.out.persistence.entity.TimeSlotEntity.TimeSlotStatusEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between TimeSlot domain model and TimeSlotEntity
 */
@Component
public class TimeSlotMapper {

    /**
     * Convert TimeSlotEntity to TimeSlot domain model
     */
    public TimeSlot toDomain(TimeSlotEntity entity) {
        if (entity == null) {
            return null;
        }

        TimeSlot timeSlot = new TimeSlot(
            entity.getDate(),
            entity.getStartTime(),
            entity.getEndTime(),
            entity.getPartySize()
        );

        timeSlot.setId(entity.getId());
        timeSlot.setStatus(mapStatusToDomain(entity.getStatus()));
        timeSlot.setCustomerName(entity.getCustomerName());
        timeSlot.setCustomerPhone(entity.getCustomerPhone());
        timeSlot.setCustomerEmail(entity.getCustomerEmail());
        timeSlot.setSpecialRequests(entity.getSpecialRequests());
        timeSlot.setCreatedAt(entity.getCreatedAt());
        timeSlot.setUpdatedAt(entity.getUpdatedAt());
        timeSlot.setVersion(entity.getVersion());

        return timeSlot;
    }

    /**
     * Convert TimeSlot domain model to TimeSlotEntity
     */
    public TimeSlotEntity toEntity(TimeSlot domain) {
        if (domain == null) {
            return null;
        }

        TimeSlotEntity entity = new TimeSlotEntity(
            domain.getDate(),
            domain.getStartTime(),
            domain.getEndTime(),
            domain.getPartySize()
        );

        entity.setId(domain.getId());
        entity.setStatus(mapStatusToEntity(domain.getStatus()));
        entity.setCustomerName(domain.getCustomerName());
        entity.setCustomerPhone(domain.getCustomerPhone());
        entity.setCustomerEmail(domain.getCustomerEmail());
        entity.setSpecialRequests(domain.getSpecialRequests());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setVersion(domain.getVersion());

        return entity;
    }

    /**
     * Update existing entity with domain model data
     */
    public void updateEntity(TimeSlotEntity entity, TimeSlot domain) {
        if (entity == null || domain == null) {
            return;
        }

        entity.setDate(domain.getDate());
        entity.setStartTime(domain.getStartTime());
        entity.setEndTime(domain.getEndTime());
        entity.setPartySize(domain.getPartySize());
        entity.setStatus(mapStatusToEntity(domain.getStatus()));
        entity.setCustomerName(domain.getCustomerName());
        entity.setCustomerPhone(domain.getCustomerPhone());
        entity.setCustomerEmail(domain.getCustomerEmail());
        entity.setSpecialRequests(domain.getSpecialRequests());
        // Note: Don't update ID, createdAt, updatedAt, version - managed by JPA
    }

    /**
     * Create TimeSlot with customer reservation data
     */
    public TimeSlot toDomainWithReservationData(TimeSlotEntity entity) {
        TimeSlot timeSlot = toDomain(entity);
        if (timeSlot != null && entity.getCustomerName() != null) {
            // Additional processing for reservation data if needed
            timeSlot.setStatus(mapStatusToDomain(entity.getStatus()));
        }
        return timeSlot;
    }

    /**
     * Map TimeSlotStatusEntity to TimeSlotStatus domain enum
     */
    private TimeSlotStatus mapStatusToDomain(TimeSlotStatusEntity entity) {
        if (entity == null) {
            return TimeSlotStatus.AVAILABLE;
        }

        return switch (entity) {
            case AVAILABLE -> TimeSlotStatus.AVAILABLE;
            case RESERVED -> TimeSlotStatus.RESERVED;
            case CONFIRMED -> TimeSlotStatus.CONFIRMED;
            case CANCELLED -> TimeSlotStatus.CANCELLED;
            case COMPLETED -> TimeSlotStatus.COMPLETED;
        };
    }

    /**
     * Map TimeSlotStatus domain enum to TimeSlotStatusEntity
     */
    public TimeSlotStatusEntity mapStatusToEntity(TimeSlotStatus domain) {
        if (domain == null) {
            return TimeSlotStatusEntity.AVAILABLE;
        }

        return switch (domain) {
            case AVAILABLE -> TimeSlotStatusEntity.AVAILABLE;
            case RESERVED -> TimeSlotStatusEntity.RESERVED;
            case CONFIRMED -> TimeSlotStatusEntity.CONFIRMED;
            case CANCELLED -> TimeSlotStatusEntity.CANCELLED;
            case COMPLETED -> TimeSlotStatusEntity.COMPLETED;
        };
    }
}