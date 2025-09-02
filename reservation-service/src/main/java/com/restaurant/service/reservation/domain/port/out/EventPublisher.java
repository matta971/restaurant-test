package com.restaurant.service.reservation.domain.port.out;

import com.restaurant.service.reservation.domain.model.Reservation;

/**
 * Port interface for publishing domain events
 */
public interface EventPublisher {

    void publishReservationCreated(Reservation reservation);
    
    void publishReservationUpdated(Reservation reservation);
    
    void publishReservationConfirmed(Reservation reservation);
    
    void publishReservationCancelled(Reservation reservation);
    
    void publishReservationCompleted(Reservation reservation);
    
    void publishReservationNoShow(Reservation reservation);
}