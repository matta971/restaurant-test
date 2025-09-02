package com.restaurant.service.restaurant.infrastructure.adapter.out.event;

import com.restaurant.service.restaurant.domain.port.out.EventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Spring Event Publisher Adapter implementation for EventPublisherPort
 * This adapter publishes domain events using Spring's ApplicationEventPublisher
 * In a production system, this could be replaced with Apache Kafka, RabbitMQ, etc.
 */
@Component
public class EventPublisherAdapter implements EventPublisherPort {

    private static final Logger logger = LoggerFactory.getLogger(EventPublisherAdapter.class);

    private final ApplicationEventPublisher applicationEventPublisher;

    public EventPublisherAdapter(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publishEvent(DomainEvent event) {
        if (event == null) {
            logger.warn("Attempted to publish null event");
            return;
        }

        try {
            logger.info("Publishing domain event: {} for aggregate {}",
                    event.getEventType(), event.getAggregateId());

            // Publish using Spring's event mechanism
            applicationEventPublisher.publishEvent(event);

            logger.debug("Successfully published event: {}", event);

        } catch (Exception e) {
            logger.error("Failed to publish domain event: {} for aggregate {}",
                    event.getEventType(), event.getAggregateId(), e);

            // In a production system, you might want to:
            // - Store failed events for retry
            // - Send to a dead letter queue
            // - Alert monitoring systems
            throw new EventPublishingException("Failed to publish event", e);
        }
    }

    /**
     * Exception thrown when event publishing fails
     */
    public static class EventPublishingException extends RuntimeException {
        public EventPublishingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}