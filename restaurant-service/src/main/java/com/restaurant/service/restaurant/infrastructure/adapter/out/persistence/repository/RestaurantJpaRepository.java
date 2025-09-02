package com.restaurant.service.restaurant.infrastructure.adapter.out.persistence.repository;

import com.restaurant.service.restaurant.infrastructure.adapter.out.persistence.entity.RestaurantEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for Restaurant entities
 */
@Repository
public interface RestaurantJpaRepository extends JpaRepository<RestaurantEntity, Long> {

    /**
     * Find restaurant by email
     */
    Optional<RestaurantEntity> findByEmail(String email);

    /**
     * Find all active restaurants
     */
    List<RestaurantEntity> findByActiveTrue();

    /**
     * Find all active restaurants with pagination
     */
    Page<RestaurantEntity> findByActiveTrue(Pageable pageable);

    /**
     * Find restaurants by name containing (case insensitive)
     */
    @Query("SELECT r FROM RestaurantEntity r WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<RestaurantEntity> findByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Find restaurants by name containing with pagination
     */
    @Query("SELECT r FROM RestaurantEntity r WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<RestaurantEntity> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    /**
     * Find restaurants by city (extracted from address)
     */
    @Query("SELECT r FROM RestaurantEntity r WHERE LOWER(r.address) LIKE LOWER(CONCAT('%', :city, '%'))")
    List<RestaurantEntity> findByCity(@Param("city") String city);

    /**
     * Find restaurants with minimum capacity
     */
    List<RestaurantEntity> findByCapacityGreaterThanEqual(Integer minCapacity);

    /**
     * Check if email exists for another restaurant
     */
    boolean existsByEmailAndIdNot(String email, Long id);

    /**
     * Count active restaurants
     */
    long countByActiveTrue();

    /**
     * Find restaurants with available tables
     */
    @Query("""
        SELECT DISTINCT r FROM RestaurantEntity r 
        JOIN r.tables t 
        WHERE t.available = true AND r.active = true
        """)
    List<RestaurantEntity> findRestaurantsWithAvailableTables();

    /**
     * Find restaurants that can accommodate party size
     */
    @Query("""
        SELECT DISTINCT r FROM RestaurantEntity r 
        JOIN r.tables t 
        WHERE t.available = true AND t.seats >= :partySize AND r.active = true
        """)
    List<RestaurantEntity> findRestaurantsByPartySize(@Param("partySize") Integer partySize);

    /**
     * Get restaurant statistics
     */
    @Query("""
        SELECT r FROM RestaurantEntity r 
        LEFT JOIN FETCH r.tables 
        WHERE r.id = :restaurantId
        """)
    Optional<RestaurantEntity> findByIdWithTables(@Param("restaurantId") Long restaurantId);
}