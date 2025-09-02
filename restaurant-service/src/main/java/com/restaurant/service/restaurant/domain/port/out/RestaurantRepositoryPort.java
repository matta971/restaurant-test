package com.restaurant.service.restaurant.domain.port.out;

import com.restaurant.service.restaurant.domain.model.Restaurant;
import java.util.List;
import java.util.Optional;

/**
 * Port (interface) for Restaurant persistence operations
 * This is part of the hexagonal architecture - defines the contract
 * that infrastructure adapters must implement
 */
public interface RestaurantRepositoryPort {

    /**
     * Saves a restaurant (create or update)
     * 
     * @param restaurant the restaurant to save
     * @return the saved restaurant with generated ID if new
     */
    Restaurant save(Restaurant restaurant);

    /**
     * Finds a restaurant by its ID
     * 
     * @param id the restaurant ID
     * @return optional containing the restaurant, empty if not found
     */
    Optional<Restaurant> findById(Long id);

    /**
     * Finds all restaurants
     * 
     * @return list of all restaurants
     */
    List<Restaurant> findAll();

    /**
     * Finds all active restaurants
     * 
     * @return list of active restaurants
     */
    List<Restaurant> findAllActive();

    /**
     * Finds restaurants by name (case-insensitive, partial match)
     * 
     * @param name the name to search for
     * @return list of matching restaurants
     */
    List<Restaurant> findByNameContaining(String name);

    /**
     * Finds restaurants by city
     * 
     * @param city the city to search in
     * @return list of restaurants in the city
     */
    List<Restaurant> findByCity(String city);

    /**
     * Checks if a restaurant exists with the given ID
     * 
     * @param id the restaurant ID
     * @return true if exists, false otherwise
     */
    boolean existsById(Long id);

    /**
     * Deletes a restaurant by ID
     * 
     * @param id the restaurant ID to delete
     */
    void deleteById(Long id);

    /**
     * Counts total number of restaurants
     * 
     * @return total count
     */
    long count();

    /**
     * Counts active restaurants
     * 
     * @return active restaurant count
     */
    long countActive();
}