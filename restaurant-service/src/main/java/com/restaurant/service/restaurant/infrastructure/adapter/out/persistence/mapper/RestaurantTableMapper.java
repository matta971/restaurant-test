package com.restaurant.service.restaurant.infrastructure.adapter.out.persistence.mapper;

import com.restaurant.service.restaurant.domain.model.RestaurantTable;
import com.restaurant.service.restaurant.domain.model.TableLocation;
import com.restaurant.service.restaurant.infrastructure.adapter.out.persistence.entity.RestaurantTableEntity;
import com.restaurant.service.restaurant.infrastructure.adapter.out.persistence.entity.RestaurantTableEntity.TableLocationEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between RestaurantTable domain model and RestaurantTableEntity
 */
@Component
public class RestaurantTableMapper {

    private final TimeSlotMapper timeSlotMapper;

    public RestaurantTableMapper(TimeSlotMapper timeSlotMapper) {
        this.timeSlotMapper = timeSlotMapper;
    }

    /**
     * Convert RestaurantTableEntity to RestaurantTable domain model
     */
    public RestaurantTable toDomain(RestaurantTableEntity entity) {
        if (entity == null) {
            return null;
        }

        RestaurantTable table = new RestaurantTable(
            entity.getSeats(),
            mapLocationToDomain(entity.getLocation())
        );

        table.setId(entity.getId());
        table.setAvailable(entity.getAvailable());
        table.setTableNumber(entity.getTableNumber());
        table.setCreatedAt(entity.getCreatedAt());
        table.setUpdatedAt(entity.getUpdatedAt());
        table.setVersion(entity.getVersion());

        // Map time slots if they are loaded (avoid lazy loading issues)
        if (entity.getTimeSlots() != null && !entity.getTimeSlots().isEmpty()) {
            entity.getTimeSlots().forEach(timeSlotEntity -> {
                table.addTimeSlot(timeSlotMapper.toDomain(timeSlotEntity));
            });
        }

        return table;
    }

    /**
     * Convert RestaurantTable domain model to RestaurantTableEntity
     */
    public RestaurantTableEntity toEntity(RestaurantTable domain) {
        if (domain == null) {
            return null;
        }

        RestaurantTableEntity entity = new RestaurantTableEntity(
            domain.getSeats(),
            mapLocationToEntity(domain.getLocation()),
            domain.getTableNumber()
        );

        entity.setId(domain.getId());
        entity.setAvailable(domain.isAvailable());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setVersion(domain.getVersion());

        // Map time slots
        if (domain.getTimeSlots() != null && !domain.getTimeSlots().isEmpty()) {
            domain.getTimeSlots().forEach(timeSlot -> {
                entity.addTimeSlot(timeSlotMapper.toEntity(timeSlot));
            });
        }

        return entity;
    }

    /**
     * Update existing entity with domain model data
     */
    public void updateEntity(RestaurantTableEntity entity, RestaurantTable domain) {
        if (entity == null || domain == null) {
            return;
        }

        entity.setSeats(domain.getSeats());
        entity.setLocation(mapLocationToEntity(domain.getLocation()));
        entity.setAvailable(domain.isAvailable());
        entity.setTableNumber(domain.getTableNumber());
        // Note: Don't update ID, createdAt, updatedAt, version - managed by JPA
    }

    /**
     * Convert RestaurantTableEntity to RestaurantTable domain model without time slots
     * Useful for avoiding lazy loading when time slots are not needed
     */
    public RestaurantTable toDomainWithoutTimeSlots(RestaurantTableEntity entity) {
        if (entity == null) {
            return null;
        }

        RestaurantTable table = new RestaurantTable(
            entity.getSeats(),
            mapLocationToDomain(entity.getLocation())
        );

        table.setId(entity.getId());
        table.setAvailable(entity.getAvailable());
        table.setTableNumber(entity.getTableNumber());
        table.setCreatedAt(entity.getCreatedAt());
        table.setUpdatedAt(entity.getUpdatedAt());
        table.setVersion(entity.getVersion());

        return table;
    }

    /**
     * Map TableLocationEntity to TableLocation domain enum
     */
    private TableLocation mapLocationToDomain(TableLocationEntity entity) {
        if (entity == null) {
            return null;
        }

        return switch (entity) {
            case WINDOW -> TableLocation.WINDOW;
            case TERRACE -> TableLocation.TERRACE;
            case INDOOR -> TableLocation.INDOOR;
            case PRIVATE_ROOM -> TableLocation.PRIVATE_ROOM;
        };
    }

    /**
     * Map TableLocation domain enum to TableLocationEntity
     */
    private TableLocationEntity mapLocationToEntity(TableLocation domain) {
        if (domain == null) {
            return null;
        }

        return switch (domain) {
            case WINDOW -> TableLocationEntity.WINDOW;
            case TERRACE -> TableLocationEntity.TERRACE;
            case INDOOR -> TableLocationEntity.INDOOR;
            case PRIVATE_ROOM -> TableLocationEntity.PRIVATE_ROOM;
        };
    }
}