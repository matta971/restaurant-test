package com.restaurant.service.reservation.infrastructure.adapter.out.persistence;

import com.restaurant.service.reservation.infrastructure.adapter.out.persistence.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA Repository interface for CustomerEntity
 * Provides data access operations for customers
 */
@Repository
public interface CustomerJpaRepository extends JpaRepository<CustomerEntity, Long> {

    /**
     * Find customer by email address
     */
    Optional<CustomerEntity> findByEmail(String email);

    /**
     * Check if customer exists by email
     */
    boolean existsByEmail(String email);

    /**
     * Find customers by first name and last name
     */
    Optional<CustomerEntity> findByFirstNameAndLastName(String firstName, String lastName);
}