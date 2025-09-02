package com.restaurant.service.restaurant.infrastructure.adapter.out.event;

import com.restaurant.service.restaurant.domain.port.out.EventPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * Adapter for publishing domain events using Spring's ApplicationEventPublisher
 * This is the infrastructure implementation of the EventPublisherPort
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SpringEventPublisherAdapter implements EventPublisherPort {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publishEvent(DomainEvent event) {
        log.info("Publishing domain event: {} for aggregate: {}", 
                event.getEventType(), event.getAggregateId());
        
        try {
            // Wrap the domain event in a Spring-compatible event
            var springEvent = new DomainEventWrapper(event);
            applicationEventPublisher.publishEvent(springEvent);
            
            log.debug("Successfully published event: {}", event.getEventType());
        } catch (Exception e) {
            log.error("Failed to publish domain event: {} for aggregate: {}", 
                    event.getEventType(), event.getAggregateId(), e);
            throw new EventPublishingException("Failed to publish domain event", e);
        }
    }

    /**
     * Wrapper to make domain events compatible with Spring's event system
     */
    public static class DomainEventWrapper {
        private final DomainEvent domainEvent;

        public DomainEventWrapper(DomainEvent domainEvent) {
            this.domainEvent = domainEvent;
        }

        public DomainEvent getDomainEvent() {
            return domainEvent;
        }

        public String getEventType() {
            return domainEvent.getEventType();
        }

        public String getAggregateId() {
            return domainEvent.getAggregateId();
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

/**
 * Configuration for event handling
 */
@Configuration
@EnableAsync
@Slf4j
class EventHandlingConfiguration {

    @Bean
    @Primary
    public TaskExecutor eventTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("event-");
        executor.initialize();
        return executor;
    }
}

/**
 * Sample event listeners for demonstration and logging
 */
@Component
@Slf4j
class DomainEventListeners {

    @EventListener
    @Async("eventTaskExecutor")
    public void handleRestaurantCreated(SpringEventPublisherAdapter.DomainEventWrapper wrapper) {
        if (wrapper.getDomainEvent() instanceof EventPublisherPort.RestaurantCreatedEvent event) {
            log.info("Handling RestaurantCreatedEvent for restaurant: {} ({})", 
                    event.restaurantName(), event.restaurantId());
            
            // Here you could:
            // - Send welcome email to restaurant owner
            // - Initialize default settings
            // - Create audit log entry
            // - Notify other services
            
            // Simulate some processing
            try {
                Thread.sleep(100);
                log.info("RestaurantCreatedEvent processed successfully");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Event processing interrupted", e);
            }
        }
    }

    @EventListener
    @Async("eventTaskExecutor")
    public void handleTableAdded(SpringEventPublisherAdapter.DomainEventWrapper wrapper) {
        if (wrapper.getDomainEvent() instanceof EventPublisherPort.TableAddedEvent event) {
            log.info("Handling TableAddedEvent: Table {} added to restaurant {}", 
                    event.tableNumber(), event.restaurantId());
            
            // Here you could:
            // - Update capacity metrics
            // - Notify reservation system
            // - Update analytics
        }
    }

    @EventListener
    @Async("eventTaskExecutor")
    public void handleReservationCreated(SpringEventPublisherAdapter.DomainEventWrapper wrapper) {
        if (wrapper.getDomainEvent() instanceof EventPublisherPort.ReservationCreatedEvent event) {
            log.info("Handling ReservationCreatedEvent: Reservation for {} people on {} at {}", 
                    event.partySize(), event.date(), event.startTime());
            
            // Here you could:
            // - Send confirmation email
            // - Update availability cache
            // - Notify kitchen/staff
            // - Update revenue projections
        }
    }

    @EventListener
    @Async("eventTaskExecutor")
    public void handleCapacityThresholdReached(SpringEventPublisherAdapter.DomainEventWrapper wrapper) {
        if (wrapper.getDomainEvent() instanceof EventPublisherPort.CapacityThresholdReachedEvent event) {
            log.warn("Capacity threshold reached for restaurant {}: {}% utilization", 
                    event.restaurantName(), event.utilizationRate() * 100);
            
            // Here you could:
            // - Send alert to restaurant manager
            // - Trigger dynamic pricing
            // - Adjust marketing campaigns
            // - Scale resources if needed
        }
    }

    @EventListener
    public void handleAllEvents(SpringEventPublisherAdapter.DomainEventWrapper wrapper) {
        // This listener receives all events for audit logging
        var event = wrapper.getDomainEvent();
        log.debug("Domain event recorded: {} for aggregate {} at {}", 
                event.getEventType(), event.getAggregateId(), event.getOccurredAt());
        
        // Here you could:
        // - Write to audit log
        // - Update event store
        // - Send to message queue for other services
    }
}