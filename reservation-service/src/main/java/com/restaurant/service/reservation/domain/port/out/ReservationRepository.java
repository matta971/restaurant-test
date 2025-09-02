package com.restaurant.service.reservation.domain.port.out;

import com.restaurant.service.reservation.domain.model.Reservation;
import com.restaurant.service.reservation.domain.model.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Port interface for reservation persistence operations
 */
public interface ReservationRepository {

    Reservation save(Reservation reservation);
    
    Optional<Reservation> findById(Long id);
    
    Page<Reservation> findAll(Pageable pageable);
    
    List<Reservation> findByCustomerEmail(String customerEmail);
    
    Page<Reservation> findByRestaurantId(Long restaurantId, Pageable pageable);
    
    List<Reservation> findByReservationDateBetween(LocalDate startDate, LocalDate endDate);
    
    List<Reservation> findByStatus(ReservationStatus status);
    
    List<Reservation> findUpcomingReservationsByCustomerEmail(String customerEmail, LocalDate fromDate);
    
    List<Reservation> findByRestaurantIdAndReservationDate(Long restaurantId, LocalDate date);
    
    void delete(Reservation reservation);
    
    boolean existsById(Long id);
    
    long count();
}