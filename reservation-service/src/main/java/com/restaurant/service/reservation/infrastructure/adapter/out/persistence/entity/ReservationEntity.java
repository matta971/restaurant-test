package com.restaurant.service.reservation.infrastructure.adapter.out.persistence.entity;

import com.restaurant.service.reservation.domain.model.Reservation;
import com.restaurant.service.reservation.domain.model.ReservationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * JPA Entity for Reservation
 * Maps reservation domain model to database table
 */
@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Column(name = "table_id", nullable = false)
    private Long tableId;

    @Column(name = "reservation_date", nullable = false)
    private LocalDate reservationDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "party_size", nullable = false)
    private Integer partySize;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReservationStatus status;

    @Column(name = "special_requests", columnDefinition = "TEXT")
    private String specialRequests;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    /**
     * Creates ReservationEntity from domain Reservation
     */
    public static ReservationEntity fromDomain(Reservation reservation) {
        ReservationEntity entity = new ReservationEntity();
        entity.setId(reservation.getId());
        entity.setCustomer(CustomerEntity.fromDomain(reservation.getCustomer()));
        entity.setRestaurantId(reservation.getRestaurantId());
        entity.setTableId(reservation.getTableId());
        entity.setReservationDate(reservation.getReservationDate());
        entity.setStartTime(reservation.getStartTime());
        entity.setEndTime(reservation.getEndTime());
        entity.setPartySize(reservation.getPartySize());
        entity.setStatus(reservation.getStatus());
        entity.setSpecialRequests(reservation.getSpecialRequests());
        entity.setCreatedAt(reservation.getCreatedAt());
        entity.setConfirmedAt(reservation.getConfirmedAt());
        entity.setCancelledAt(reservation.getCancelledAt());
        entity.setCancellationReason(reservation.getCancellationReason());
        return entity;
    }

    /**
     * Converts entity to domain Reservation
     */
    public Reservation toDomain() {
        Reservation reservation = new Reservation(
                customer.toDomain(),
                restaurantId,
                tableId,
                reservationDate,
                startTime,
                endTime,
                partySize
        );
        
        reservation.setId(id);
        reservation.setStatus(status);
        reservation.setSpecialRequests(specialRequests);
        reservation.setConfirmedAt(confirmedAt);
        reservation.setCancelledAt(cancelledAt);
        reservation.setCancellationReason(cancellationReason);
        
        return reservation;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = ReservationStatus.PENDING;
        }
    }
}