package com.restaurant.service.restaurant.infrastructure.adapter.out.persistence;

import com.restaurant.service.restaurant.domain.model.Restaurant;
import com.restaurant.service.restaurant.infrastructure.adapter.out.persistence.entity.RestaurantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for Restaurant entities
 * This is the actual JPA repository interface that Spring Data will implement
 */
@Repository
public interface RestaurantJpaAdapter extends JpaRepository<RestaurantEntity, Long> {

    /**
     * Finds all active restaurants
     *
     * @return list of active restaurants
     */
    @Query("SELECT r FROM RestaurantEntity r WHERE r.active = true ORDER BY r.name")
    List<RestaurantEntity> findAllActive();

    /**
     * Finds restaurants by name (case-insensitive, partial match)
     *
     * @param name the name to search for
     * @return list of matching restaurants
     */
    @Query("SELECT r FROM RestaurantEntity r WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%')) ORDER BY r.name")
    List<RestaurantEntity> findByNameContaining(@Param("name") String name);

    /**
     * Finds restaurants by city (extracted from address)
     *
     * @param city the city to search in
     * @return list of restaurants in the city
     */
    @Query("SELECT r FROM RestaurantEntity r WHERE LOWER(r.address) LIKE LOWER(CONCAT('%', :city, '%')) ORDER BY r.name")
    List<RestaurantEntity> findByCity(@Param("city") String city);

    /**
     * Counts active restaurants
     *
     * @return active restaurant count
     */
    @Query("SELECT COUNT(r) FROM RestaurantEntity r WHERE r.active = true")
    long countActive();

    /**
     * Finds all restaurants ordered by name
     *
     * @return list of all restaurants ordered by name
     */
    @Override
    @Query("SELECT r FROM RestaurantEntity r ORDER BY r.name")
    List<RestaurantEntity> findAll();
}