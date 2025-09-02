package com.restaurant.service.restaurant.domain.port.out;

/**
 * Port for publishing domain events to external systems
 * This allows the restaurant service to communicate with other bounded contexts
 * without direct coupling
 */
public interface EventPublisherPort {

    /**
     * Publishes a domain event
     * 
     * @param event the event to publish
     */
    void publishEvent(DomainEvent event);

    /**
     * Base interface for all domain events
     */
    interface DomainEvent {
        String getEventType();
        Long getAggregateId();
        java.time.Instant getOccurredAt();
    }

    /**
     * Event fired when a restaurant is created
     */
    record RestaurantCreatedEvent(
        Long restaurantId,
        String name,
        String address,
        Integer capacity,
        java.time.Instant occurredAt
    ) implements DomainEvent {
        @Override
        public String getEventType() {
            return "RestaurantCreated";
        }

        @Override
        public Long getAggregateId() {
            return restaurantId;
        }

        @Override
        public java.time.Instant getOccurredAt() {
            return occurredAt;
        }
    }

    /**
     * Event fired when a restaurant is activated/deactivated
     */
    record RestaurantStatusChangedEvent(
        Long restaurantId,
        boolean active,
        java.time.Instant occurredAt
    ) implements DomainEvent {
        @Override
        public String getEventType() {
            return "RestaurantStatusChanged";
        }

        @Override
        public Long getAggregateId() {
            return restaurantId;
        }

        @Override
        public java.time.Instant getOccurredAt() {
            return occurredAt;
        }
    }

    /**
     * Event fired when a table is added to a restaurant
     */
    record TableAddedEvent(
        Long restaurantId,
        Long tableId,
        String tableNumber,
        Integer seats,
        String location,
        java.time.Instant occurredAt
    ) implements DomainEvent {
        @Override
        public String getEventType() {
            return "TableAdded";
        }

        @Override
        public Long getAggregateId() {
            return restaurantId;
        }

        @Override
        public java.time.Instant getOccurredAt() {
            return occurredAt;
        }
    }

    /**
     * Event fired when a table's availability changes
     */
    record TableAvailabilityChangedEvent(
        Long restaurantId,
        Long tableId,
        String tableNumber,
        boolean available,
        java.time.Instant occurredAt
    ) implements DomainEvent {
        @Override
        public String getEventType() {
            return "TableAvailabilityChanged";
        }

        @Override
        public Long getAggregateId() {
            return restaurantId;
        }

        @Override
        public java.time.Instant getOccurredAt() {
            return occurredAt;
        }
    }

    /**
     * Event fired when a reservation is created
     */
    record ReservationCreatedEvent(
        Long restaurantId,
        Long tableId,
        Long timeSlotId,
        java.time.LocalDate date,
        java.time.LocalTime startTime,
        java.time.LocalTime endTime,
        Integer partySize,
        java.time.Instant occurredAt
    ) implements DomainEvent {
        @Override
        public String getEventType() {
            return "ReservationCreated";
        }

        @Override
        public Long getAggregateId() {
            return restaurantId;
        }

        @Override
        public java.time.Instant getOccurredAt() {
            return occurredAt;
        }
    }

    /**
     * Event fired when a reservation status changes
     */
    record ReservationStatusChangedEvent(
        Long restaurantId,
        Long tableId,
        Long timeSlotId,
        String previousStatus,
        String newStatus,
        java.time.Instant occurredAt
    ) implements DomainEvent {
        @Override
        public String getEventType() {
            return "ReservationStatusChanged";
        }

        @Override
        public Long getAggregateId() {
            return restaurantId;
        }

        @Override
        public java.time.Instant getOccurredAt() {
            return occurredAt;
        }
    }
}