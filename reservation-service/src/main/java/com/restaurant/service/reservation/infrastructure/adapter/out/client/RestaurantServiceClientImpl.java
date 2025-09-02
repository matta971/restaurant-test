package com.restaurant.service.reservation.infrastructure.adapter.out.client;

import com.restaurant.service.reservation.domain.port.out.RestaurantServiceClient;
import com.restaurant.service.reservation.infrastructure.adapter.out.client.config.FeignConfig;
import com.restaurant.service.reservation.infrastructure.adapter.out.dto.RestaurantDto;
import com.restaurant.service.reservation.infrastructure.adapter.out.dto.TableDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Feign client implementation for Restaurant Service communication
 * Handles HTTP communication with Restaurant Service
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RestaurantServiceClientImpl implements RestaurantServiceClient {

    private final RestaurantServiceFeignClient feignClient;

    @Override
    public RestaurantInfo getRestaurant(Long restaurantId) {
        log.debug("Fetching restaurant info for ID: {}", restaurantId);
        
        try {
            RestaurantDto dto = feignClient.getRestaurant(restaurantId);
            return new RestaurantInfo(
                    dto.id(),
                    dto.name(),
                    dto.address(),
                    dto.phoneNumber(),
                    dto.email(),
                    dto.capacity(),
                    dto.active()
            );
        } catch (Exception e) {
            log.error("Failed to fetch restaurant info for ID: {}", restaurantId, e);
            throw new RestaurantServiceException("Failed to fetch restaurant info", e);
        }
    }

    @Override
    public boolean isTableAvailable(Long restaurantId, Long tableId, LocalDate date, 
                                   LocalTime startTime, LocalTime endTime) {
        log.debug("Checking table availability: restaurant={}, table={}, date={}, time={}-{}", 
                restaurantId, tableId, date, startTime, endTime);
        
        try {
            return feignClient.checkTableAvailability(restaurantId, tableId, date, startTime, endTime);
        } catch (Exception e) {
            log.error("Failed to check table availability", e);
            return false; // Fail-safe: assume not available
        }
    }

    @Override
    public List<TableInfo> getAvailableTables(Long restaurantId, LocalDate date,
                                            LocalTime startTime, LocalTime endTime, Integer partySize) {
        log.debug("Fetching available tables: restaurant={}, date={}, time={}-{}, partySize={}", 
                restaurantId, date, startTime, endTime, partySize);
        
        try {
            List<TableDto> tables = feignClient.getAvailableTables(restaurantId, date, startTime, endTime, partySize);
            return tables.stream()
                    .map(dto -> new TableInfo(
                            dto.id(),
                            dto.restaurantId(),
                            dto.seats(),
                            dto.location(),
                            dto.available()
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to fetch available tables", e);
            throw new RestaurantServiceException("Failed to fetch available tables", e);
        }
    }

    @Override
    public boolean reserveTable(Long restaurantId, Long tableId, LocalDate date,
                               LocalTime startTime, LocalTime endTime) {
        log.debug("Reserving table: restaurant={}, table={}, date={}, time={}-{}", 
                restaurantId, tableId, date, startTime, endTime);
        
        try {
            feignClient.reserveTable(restaurantId, tableId, date, startTime, endTime);
            return true;
        } catch (Exception e) {
            log.error("Failed to reserve table", e);
            return false;
        }
    }

    @Override
    public boolean releaseTableReservation(Long restaurantId, Long tableId, LocalDate date,
                                         LocalTime startTime, LocalTime endTime) {
        log.debug("Releasing table reservation: restaurant={}, table={}, date={}, time={}-{}", 
                restaurantId, tableId, date, startTime, endTime);
        
        try {
            feignClient.releaseTableReservation(restaurantId, tableId, date, startTime, endTime);
            return true;
        } catch (Exception e) {
            log.error("Failed to release table reservation", e);
            return false;
        }
    }

    /**
     * Feign client interface for Restaurant Service
     */
    @FeignClient(
            name = "restaurant-service",
            url = "${restaurant-service.url:http://localhost:8081}",
            configuration = FeignConfig.class
    )
    public interface RestaurantServiceFeignClient {

        @GetMapping("/api/restaurants/{id}")
        RestaurantDto getRestaurant(@PathVariable("id") Long restaurantId);

        @GetMapping("/api/restaurants/{restaurantId}/tables/{tableId}/availability")
        boolean checkTableAvailability(
                @PathVariable Long restaurantId,
                @PathVariable Long tableId,
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime
        );

        @GetMapping("/api/restaurants/{restaurantId}/tables/available")
        List<TableDto> getAvailableTables(
                @PathVariable Long restaurantId,
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
                @RequestParam Integer partySize
        );

        @PostMapping("/api/restaurants/{restaurantId}/tables/{tableId}/reserve")
        void reserveTable(
                @PathVariable Long restaurantId,
                @PathVariable Long tableId,
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime
        );

        @DeleteMapping("/api/restaurants/{restaurantId}/tables/{tableId}/reserve")
        void releaseTableReservation(
                @PathVariable Long restaurantId,
                @PathVariable Long tableId,
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime
        );
    }
}