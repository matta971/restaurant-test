package com.restaurant.service.reservation.infrastructure.adapter.out.event;

import com.restaurant.service.reservation.domain.model.Reservation;
import com.restaurant.service.reservation.domain.port.out.EventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Implementation of EventPublisher using Spring's ApplicationEventPublisher
 */
@Component
@Slf4j
public class EventPublisherImpl implements EventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public EventPublisherImpl(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publishReservationCreated(Reservation reservation) {
        log.info("Publishing ReservationCreated event for reservation ID: {}", reservation.getId());
        // For now, just log. In real implementation, publish to message queue
    }

    @Override
    public void publishReservationUpdated(Reservation reservation) {
        log.info("Publishing ReservationUpdated event for reservation ID: {}", reservation.getId());
    }

    @Override
    public void publishReservationConfirmed(Reservation reservation) {
        log.info("Publishing ReservationConfirmed event for reservation ID: {}", reservation.getId());
    }

    @Override
    public void publishReservationCancelled(Reservation reservation) {
        log.info("Publishing ReservationCancelled event for reservation ID: {}", reservation.getId());
    }

    @Override
    public void publishReservationCompleted(Reservation reservation) {
        log.info("Publishing ReservationCompleted event for reservation ID: {}", reservation.getId());
    }

    @Override
    public void publishReservationNoShow(Reservation reservation) {
        log.info("Publishing ReservationNoShow event for reservation ID: {}", reservation.getId());
    }
}
