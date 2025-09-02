package com.restaurant.service.restaurant.infrastructure.adapter.out.event;

import com.restaurant.service.restaurant.domain.port.out.EventPublisherPort;
import org.springframework.stereotype.Component;

/**
 * Implementation of EventPublisherPort for development
 * In production, this would integrate with a message broker like RabbitMQ, Kafka, etc.
 */
@Component
public class EventPublisher implements EventPublisherPort {

    @Override
    public void publishEvent(DomainEvent event) {
        // For now, just log the event
        System.out.println("Publishing event: " + event.getEventType() + 
                          " for aggregate: " + event.getAggregateId() + 
                          " at: " + event.getOccurredAt());
        
        // In a real implementation, you would:
        // - Serialize the event
        // - Send to message broker
        // - Handle failures and retries
        // - Implement outbox pattern for reliability
    }
}