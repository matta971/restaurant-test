package com.restaurant.service.restaurant.infrastructure.adapter.out.mock;

import com.restaurant.service.restaurant.domain.port.out.EventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Mock implementation of EventPublisherPort for testing and development
 * Simply logs events instead of actually publishing them
 */
@Component
@ConditionalOnProperty(name = "restaurant.events.publisher", havingValue = "mock", matchIfMissing = true)
public class MockEventPublisher implements EventPublisherPort {

    private static final Logger logger = LoggerFactory.getLogger(MockEventPublisher.class);

    @Override
    public void publishEvent(DomainEvent event) {
        logger.info("Publishing domain event: {} for aggregate: {} at: {}",
                event.getEventType(),
                event.getAggregateId(),
                event.getOccurredAt());

        // In a real implementation, this would publish to a message broker
        // For now, we just log the event
        logEventDetails(event);
    }

    private void logEventDetails(DomainEvent event) {
        if (event instanceof EventPublisherPort.RestaurantCreatedEvent e) {
            logger.info("Restaurant created: {} at {} with capacity {}",
                    e.restaurantName(), e.address(), e.capacity());
        } else if (event instanceof EventPublisherPort.RestaurantStatusChangedEvent e) {
            logger.info("Restaurant {} status changed to: {}",
                    e.restaurantId(), e.isActive() ? "ACTIVE" : "INACTIVE");
        } else if (event instanceof EventPublisherPort.TableAddedEvent e) {
            logger.info("Table {} added to restaurant {} with {} seats at {}",
                    e.tableNumber(), e.restaurantId(), e.seats(), e.location());
        } else if (event instanceof EventPublisherPort.TableAvailabilityChangedEvent e) {
            logger.info("Table {} availability changed to: {}",
                    e.tableNumber(), e.isAvailable() ? "AVAILABLE" : "UNAVAILABLE");
        } else if (event instanceof EventPublisherPort.ReservationCreatedEvent e) {
            logger.info("Reservation created for {} people on {} from {} to {} at table {}",
                    e.partySize(), e.date(), e.startTime(), e.endTime(), e.tableId());
        } else if (event instanceof EventPublisherPort.ReservationStatusChangedEvent e) {
            logger.info("Reservation {} status changed from {} to {}",
                    e.timeSlotId(), e.previousStatus(), e.newStatus());
        } else {
            logger.info("Unknown event type: {}", event.getEventType());
        }
    }
}