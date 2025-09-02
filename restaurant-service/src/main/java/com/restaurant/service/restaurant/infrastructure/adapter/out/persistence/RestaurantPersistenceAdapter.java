package com.restaurant.service.restaurant.infrastructure.adapter.out.persistence;

import com.restaurant.service.restaurant.domain.model.Restaurant;
import com.restaurant.service.restaurant.domain.port.out.RestaurantRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Concrete adapter that implements RestaurantRepositoryPort
 * This adapter delegates to the Spring Data JPA repository
 * Following hexagonal architecture principles
 */
@Component
public class RestaurantPersistenceAdapter implements RestaurantRepositoryPort {

    private final RestaurantJpaAdapter restaurantJpaRepository;

    public RestaurantPersistenceAdapter(RestaurantJpaAdapter restaurantJpaRepository) {
        this.restaurantJpaRepository = restaurantJpaRepository;
    }

    @Override
    public Restaurant save(Restaurant restaurant) {
        return restaurantJpaRepository.save(restaurant);
    }

    @Override
    public Optional<Restaurant> findById(Long id) {
        return restaurantJpaRepository.findById(id);
    }

    @Override
    public List<Restaurant> findAll() {
        return restaurantJpaRepository.findAll();
    }

    @Override
    public List<Restaurant> findAllActive() {
        return restaurantJpaRepository.findAllActive();
    }

    @Override
    public List<Restaurant> findByNameContaining(String name) {
        return restaurantJpaRepository.findByNameContaining(name);
    }

    @Override
    public List<Restaurant> findByCity(String city) {
        return restaurantJpaRepository.findByCity(city);
    }

    @Override
    public boolean existsById(Long id) {
        return restaurantJpaRepository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        restaurantJpaRepository.deleteById(id);
    }

    @Override
    public long count() {
        return restaurantJpaRepository.count();
    }

    @Override
    public long countActive() {
        return restaurantJpaRepository.countActive();
    }
}