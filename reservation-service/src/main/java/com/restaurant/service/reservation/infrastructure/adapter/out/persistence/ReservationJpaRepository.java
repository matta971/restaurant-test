package com.restaurant.service.reservation.infrastructure.adapter.out.persistence;

import com.restaurant.service.reservation.domain.model.ReservationStatus;
import com.restaurant.service.reservation.infrastructure.adapter.out.persistence.entity.ReservationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * JPA Repository interface for ReservationEntity
 * Provides data access operations for reservations
 */
@Repository
public interface ReservationJpaRepository extends JpaRepository<ReservationEntity, Long> {

    /**
     * Find reservations by customer email ordered by date desc and time desc
     */
    @Query("SELECT r FROM ReservationEntity r JOIN r.customer c WHERE c.email = :email ORDER BY r.reservationDate DESC, r.startTime DESC")
    List<ReservationEntity> findByCustomerEmailOrderByReservationDateDescStartTimeDesc(@Param("email") String email);

    /**
     * Find reservations by restaurant ID with pagination
     */
    Page<ReservationEntity> findByRestaurantIdOrderByReservationDateDescStartTimeDesc(Long restaurantId, Pageable pageable);

    /**
     * Find reservations between dates
     */
    List<ReservationEntity> findByReservationDateBetweenOrderByReservationDateAscStartTimeAsc(LocalDate startDate, LocalDate endDate);

    /**
     * Find reservations by status
     */
    List<ReservationEntity> findByStatusOrderByReservationDateDescStartTimeDesc(ReservationStatus status);

    /**
     * Find upcoming reservations by customer email and status
     */
    @Query("SELECT r FROM ReservationEntity r JOIN r.customer c WHERE c.email = :email AND r.reservationDate >= :fromDate AND r.status IN :statuses ORDER BY r.reservationDate ASC, r.startTime ASC")
    List<ReservationEntity> findByCustomerEmailAndReservationDateGreaterThanEqualAndStatusInOrderByReservationDateAscStartTimeAsc(
            @Param("email") String email, 
            @Param("fromDate") LocalDate fromDate, 
            @Param("statuses") List<ReservationStatus> statuses);

    /**
     * Find reservations by restaurant and date
     */
    List<ReservationEntity> findByRestaurantIdAndReservationDateOrderByStartTimeAsc(Long restaurantId, LocalDate date);

    /**
     * Check if table is available at specific time
     */
    @Query("SELECT COUNT(r) FROM ReservationEntity r WHERE r.tableId = :tableId AND r.reservationDate = :date " +
           "AND r.status IN ('PENDING', 'CONFIRMED') " +
           "AND ((r.startTime <= :startTime AND r.endTime > :startTime) OR " +
           "(r.startTime < :endTime AND r.endTime >= :endTime) OR " +
           "(r.startTime >= :startTime AND r.endTime <= :endTime))")
    long countConflictingReservations(@Param("tableId") Long tableId, 
                                    @Param("date") LocalDate date,
                                    @Param("startTime") java.time.LocalTime startTime, 
                                    @Param("endTime") java.time.LocalTime endTime);
}