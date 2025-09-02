
package com.restaurant.service.reservation.infrastructure.adapter.out.persistence;

import com.restaurant.service.reservation.domain.model.Reservation;
import com.restaurant.service.reservation.domain.model.ReservationStatus;
import com.restaurant.service.reservation.domain.port.out.ReservationRepository;
import com.restaurant.service.reservation.infrastructure.adapter.out.persistence.entity.ReservationEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JPA implementation of ReservationRepository
 * Handles persistence operations for reservations
 */
@Repository
@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepository {

    private final ReservationJpaRepository jpaRepository;

    @Override
    public Reservation save(Reservation reservation) {
        ReservationEntity entity = ReservationEntity.fromDomain(reservation);
        ReservationEntity savedEntity = jpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return jpaRepository.findById(id)
                .map(ReservationEntity::toDomain);
    }

    @Override
    public Page<Reservation> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable)
                .map(ReservationEntity::toDomain);
    }

    @Override
    public List<Reservation> findByCustomerEmail(String customerEmail) {
        return jpaRepository.findByCustomerEmailOrderByReservationDateDescStartTimeDesc(customerEmail)
                .stream()
                .map(ReservationEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Page<Reservation> findByRestaurantId(Long restaurantId, Pageable pageable) {
        return jpaRepository.findByRestaurantIdOrderByReservationDateDescStartTimeDesc(restaurantId, pageable)
                .map(ReservationEntity::toDomain);
    }

    @Override
    public List<Reservation> findByReservationDateBetween(LocalDate startDate, LocalDate endDate) {
        return jpaRepository.findByReservationDateBetweenOrderByReservationDateAscStartTimeAsc(startDate, endDate)
                .stream()
                .map(ReservationEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Reservation> findByStatus(ReservationStatus status) {
        return jpaRepository.findByStatusOrderByReservationDateDescStartTimeDesc(status)
                .stream()
                .map(ReservationEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Reservation> findUpcomingReservationsByCustomerEmail(String customerEmail, LocalDate fromDate) {
        return jpaRepository.findByCustomerEmailAndReservationDateGreaterThanEqualAndStatusInOrderByReservationDateAscStartTimeAsc(
                customerEmail, fromDate, List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED))
                .stream()
                .map(ReservationEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Reservation> findByRestaurantIdAndReservationDate(Long restaurantId, LocalDate date) {
        return jpaRepository.findByRestaurantIdAndReservationDateOrderByStartTimeAsc(restaurantId, date)
                .stream()
                .map(ReservationEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Reservation reservation) {
        jpaRepository.deleteById(reservation.getId());
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }
}