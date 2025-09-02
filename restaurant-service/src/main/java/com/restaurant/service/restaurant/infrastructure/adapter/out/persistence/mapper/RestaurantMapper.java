package com.restaurant.service.restaurant.infrastructure.adapter.out.persistence.mapper;

import com.restaurant.service.restaurant.domain.model.Restaurant;
import com.restaurant.service.restaurant.infrastructure.adapter.out.persistence.entity.RestaurantEntity;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Mapper for converting between Restaurant domain model and RestaurantEntity
 */
@Component
public class RestaurantMapper {

    private final RestaurantTableMapper tableMapper;

    public RestaurantMapper(RestaurantTableMapper tableMapper) {
        this.tableMapper = tableMapper;
    }

    /**
     * Convert RestaurantEntity to Restaurant domain model
     */
    public Restaurant toDomain(RestaurantEntity entity) {
        if (entity == null) {
            return null;
        }

        Restaurant restaurant = new Restaurant(
            entity.getName(),
            entity.getAddress(),
            entity.getPhone(),
            entity.getEmail(),
            entity.getCapacity()
        );

        restaurant.setId(entity.getId());
        restaurant.setActive(entity.getActive());
        restaurant.setOpeningTime(entity.getOpeningTime());
        restaurant.setClosingTime(entity.getClosingTime());
        restaurant.setCreatedAt(entity.getCreatedAt());
        restaurant.setUpdatedAt(entity.getUpdatedAt());
        restaurant.setVersion(entity.getVersion());

        // Map tables if they are loaded (avoid lazy loading issues)
        if (entity.getTables() != null && !entity.getTables().isEmpty()) {
            entity.getTables().forEach(tableEntity -> {
                restaurant.addTable(tableMapper.toDomain(tableEntity));
            });
        }

        return restaurant;
    }

    /**
     * Convert Restaurant domain model to RestaurantEntity
     */
    public RestaurantEntity toEntity(Restaurant domain) {
        if (domain == null) {
            return null;
        }

        RestaurantEntity entity = new RestaurantEntity(
            domain.getName(),
            domain.getAddress(),
            domain.getPhoneNumber(),
            domain.getEmail(),
            domain.getCapacity()
        );

        entity.setId(domain.getId());
        entity.setActive(domain.isActive());
        entity.setOpeningTime(domain.getOpeningTime());
        entity.setClosingTime(domain.getClosingTime());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setVersion(domain.getVersion());

        // Map tables
        if (domain.getTables() != null && !domain.getTables().isEmpty()) {
            domain.getTables().forEach(table -> {
                entity.addTable(tableMapper.toEntity(table));
            });
        }

        return entity;
    }

    /**
     * Update existing entity with domain model data
     */
    public void updateEntity(RestaurantEntity entity, Restaurant domain) {
        if (entity == null || domain == null) {
            return;
        }

        entity.setName(domain.getName());
        entity.setAddress(domain.getAddress());
        entity.setPhone(domain.getPhoneNumber());
        entity.setEmail(domain.getEmail());
        entity.setCapacity(domain.getCapacity());
        entity.setActive(domain.isActive());
        entity.setOpeningTime(domain.getOpeningTime());
        entity.setClosingTime(domain.getClosingTime());
        // Note: Don't update ID, createdAt, updatedAt, version - managed by JPA
    }

    /**
     * Convert RestaurantEntity to Restaurant domain model without tables
     * Useful for avoiding lazy loading when tables are not needed
     */
    public Restaurant toDomainWithoutTables(RestaurantEntity entity) {
        if (entity == null) {
            return null;
        }

        Restaurant restaurant = new Restaurant(
            entity.getName(),
            entity.getAddress(),
            entity.getPhone(),
            entity.getEmail(),
            entity.getCapacity()
        );

        restaurant.setId(entity.getId());
        restaurant.setActive(entity.getActive());
        restaurant.setOpeningTime(entity.getOpeningTime());
        restaurant.setClosingTime(entity.getClosingTime());
        restaurant.setCreatedAt(entity.getCreatedAt());
        restaurant.setUpdatedAt(entity.getUpdatedAt());
        restaurant.setVersion(entity.getVersion());

        return restaurant;
    }
}