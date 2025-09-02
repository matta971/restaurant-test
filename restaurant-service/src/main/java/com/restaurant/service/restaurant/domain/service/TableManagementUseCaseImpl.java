package com.restaurant.service.restaurant.domain.service;

import com.restaurant.service.restaurant.domain.model.RestaurantTable;
import com.restaurant.service.restaurant.domain.model.TableLocation;
import com.restaurant.service.restaurant.domain.port.in.TableManagementUseCase;
import com.restaurant.service.restaurant.domain.port.out.RestaurantRepositoryPort;
import com.restaurant.service.restaurant.domain.port.out.RestaurantTableRepositoryPort;
import com.restaurant.service.restaurant.domain.port.out.EventPublisherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Implementation of Table Management Use Case
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TableManagementUseCaseImpl implements TableManagementUseCase {

    private final RestaurantTableRepositoryPort tableRepository;
    private final RestaurantRepositoryPort restaurantRepository;
    private final EventPublisherPort eventPublisher;

    @Override
    public RestaurantTable createTable(CreateTableCommand command) {
        var restaurant = restaurantRepository.findById(command.restaurantId())
                .orElseThrow(() -> new RuntimeException("Restaurant not found: " + command.restaurantId()));

        var table = new RestaurantTable(command.seats(), command.location());
        restaurant.addTable(table);

        var savedTable = tableRepository.save(table);

        // Publish domain event
        var event = new EventPublisherPort.TableAddedEvent(
                command.restaurantId(),
                savedTable.getId(),
                savedTable.getTableNumber(),
                savedTable.getSeats(),
                savedTable.getLocation().name(),
                Instant.now()
        );
        eventPublisher.publishEvent(event);

        return savedTable;
    }

    @Override
    public RestaurantTable updateTable(UpdateTableCommand command) {
        var table = tableRepository.findById(command.id())
                .orElseThrow(() -> new TableNotFoundException(command.id()));

        table.setSeats(command.seats());
        table.setLocation(command.location());

        return tableRepository.save(table);
    }

    @Override
    @Transactional(readOnly = true)
    public RestaurantTable getTable(Long tableId) {
        return tableRepository.findById(tableId)
                .orElseThrow(() -> new TableNotFoundException(tableId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantTable> getRestaurantTables(Long restaurantId) {
        return tableRepository.findByRestaurantId(restaurantId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantTable> getAvailableTables(Long restaurantId) {
        return tableRepository.findAvailableByRestaurantId(restaurantId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantTable> getTablesByLocation(Long restaurantId, TableLocation location) {
        return tableRepository.findByRestaurantIdAndLocation(restaurantId, location);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantTable> getTablesByPartySize(Long restaurantId, Integer partySize) {
        return tableRepository.findByRestaurantIdAndSeatsGreaterThanEqual(restaurantId, partySize);
    }

    @Override
    public RestaurantTable makeTableAvailable(Long tableId) {
        var table = tableRepository.findById(tableId)
                .orElseThrow(() -> new TableNotFoundException(tableId));

        table.makeAvailable();
        var savedTable = tableRepository.save(table);

        // Publish domain event
        var event = new EventPublisherPort.TableAvailabilityChangedEvent(
                table.getRestaurant() != null ? table.getRestaurant().getId() : null,
                savedTable.getId(),
                savedTable.getTableNumber(),
                true,
                Instant.now()
        );
        eventPublisher.publishEvent(event);

        return savedTable;
    }

    @Override
    public RestaurantTable makeTableUnavailable(Long tableId) {
        var table = tableRepository.findById(tableId)
                .orElseThrow(() -> new TableNotFoundException(tableId));

        table.makeUnavailable();
        var savedTable = tableRepository.save(table);

        // Publish domain event
        var event = new EventPublisherPort.TableAvailabilityChangedEvent(
                table.getRestaurant() != null ? table.getRestaurant().getId() : null,
                savedTable.getId(),
                savedTable.getTableNumber(),
                false,
                Instant.now()
        );
        eventPublisher.publishEvent(event);

        return savedTable;
    }

    @Override
    public void deleteTable(Long tableId) {
        if (!tableRepository.existsById(tableId)) {
            throw new TableNotFoundException(tableId);
        }

        tableRepository.deleteById(tableId);
    }

    @Override
    @Transactional(readOnly = true)
    public TableUtilizationStats getTableUtilization(Long restaurantId) {
        long totalTables = tableRepository.countByRestaurantId(restaurantId);
        long availableTables = tableRepository.countAvailableByRestaurantId(restaurantId);
        long unavailableTables = totalTables - availableTables;

        var tables = tableRepository.findByRestaurantId(restaurantId);
        int totalSeats = tables.stream().mapToInt(RestaurantTable::getSeats).sum();

        double availabilityRate = totalTables > 0 ? (double) availableTables / totalTables : 0.0;

        return new TableUtilizationStats(
                restaurantId,
                totalTables,
                availableTables,
                unavailableTables,
                totalSeats,
                availabilityRate
        );
    }
}