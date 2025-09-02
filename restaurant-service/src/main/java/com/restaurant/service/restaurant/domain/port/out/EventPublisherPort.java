package com.restaurant.service.restaurant.domain.port.out;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Port for publishing domain events to external systems
 * This allows the domain to notify other bounded contexts about important events
 */
public interface EventPublisherPort {

    /**
     * Publishes a domain event
     *
     * @param event the domain event to publish
     */
    void publishEvent(DomainEvent event);

    /**
     * Base interface for all domain events
     */
    interface DomainEvent {
        String getEventType();
        Instant getOccurredAt();
        String getAggregateId();
    }

    /**
     * Event published when a restaurant is created
     */
    record RestaurantCreatedEvent(
            Long restaurantId,
            String restaurantName,
            String address,
            String email,
            Integer capacity,
            Instant occurredAt
    ) implements DomainEvent {
        @Override
        public String getEventType() {
            return "RESTAURANT_CREATED";
        }

        @Override
        public Instant getOccurredAt() {
            return occurredAt;
        }

        @Override
        public String getAggregateId() {
            return restaurantId.toString();
        }
    }

    /**
     * Event published when a restaurant is updated
     */
    record RestaurantUpdatedEvent(
            Long restaurantId,
            String restaurantName,
            String previousName,
            Instant occurredAt
    ) implements DomainEvent {
        @Override
        public String getEventType() {
            return "RESTAURANT_UPDATED";
        }

        @Override
        public Instant getOccurredAt() {
            return occurredAt;
        }

        @Override
        public String getAggregateId() {
            return restaurantId.toString();
        }
    }

    /**
     * Event published when a restaurant is activated or deactivated
     */
    record RestaurantStatusChangedEvent(
            Long restaurantId,
            String restaurantName,
            boolean isActive,
            Instant occurredAt
    ) implements DomainEvent {
        @Override
        public String getEventType() {
            return "RESTAURANT_STATUS_CHANGED";
        }

        @Override
        public Instant getOccurredAt() {
            return occurredAt;
        }

        @Override
        public String getAggregateId() {
            return restaurantId.toString();
        }
    }

    /**
     * Event published when a table is added to a restaurant
     */
    record TableAddedEvent(
            Long restaurantId,
            Long tableId,
            String tableNumber,
            Integer seats,
            String location,
            Instant occurredAt
    ) implements DomainEvent {
        @Override
        public String getEventType() {
            return "TABLE_ADDED";
        }

        @Override
        public Instant getOccurredAt() {
            return occurredAt;
        }

        @Override
        public String getAggregateId() {
            return restaurantId.toString();
        }
    }

    /**
     * Event published when table availability changes
     */
    record TableAvailabilityChangedEvent(
            Long restaurantId,
            Long tableId,
            String tableNumber,
            boolean isAvailable,
            Instant occurredAt
    ) implements DomainEvent {
        @Override
        public String getEventType() {
            return "TABLE_AVAILABILITY_CHANGED";
        }

        @Override
        public Instant getOccurredAt() {
            return occurredAt;
        }

        @Override
        public String getAggregateId() {
            return restaurantId.toString();
        }
    }

    /**
     * Event published when a reservation is created
     */
    record ReservationCreatedEvent(
            Long restaurantId,
            Long tableId,
            Long timeSlotId,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            Integer partySize,
            String customerEmail,
            Instant occurredAt
    ) implements DomainEvent {
        @Override
        public String getEventType() {
            return "RESERVATION_CREATED";
        }

        @Override
        public Instant getOccurredAt() {
            return occurredAt;
        }

        @Override
        public String getAggregateId() {
            return restaurantId.toString();
        }
    }

    /**
     * Event published when a reservation status changes
     */
    record ReservationStatusChangedEvent(
            Long restaurantId,
            Long tableId,
            Long timeSlotId,
            String previousStatus,
            String newStatus,
            Instant occurredAt
    ) implements DomainEvent {
        @Override
        public String getEventType() {
            return "RESERVATION_STATUS_CHANGED";
        }

        @Override
        public Instant getOccurredAt() {
            return occurredAt;
        }

        @Override
        public String getAggregateId() {
            return restaurantId.toString();
        }
    }

    /**
     * Event published when capacity utilization reaches a threshold
     */
    record CapacityThresholdReachedEvent(
            Long restaurantId,
            String restaurantName,
            LocalDate date,
            LocalTime time,
            double utilizationRate,
            double threshold,
            Instant occurredAt
    ) implements DomainEvent {
        @Override
        public String getEventType() {
            return "CAPACITY_THRESHOLD_REACHED";
        }

        @Override
        public Instant getOccurredAt() {
            return occurredAt;
        }

        @Override
        public String getAggregateId() {
            return restaurantId.toString();
        }
    }
}