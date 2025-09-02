package com.restaurant.service.reservation.domain.port.out;

import com.restaurant.service.reservation.domain.model.Customer;

import java.util.List;
import java.util.Optional;

/**
 * Port interface for customer persistence operations
 */
public interface CustomerRepository {

    Customer save(Customer customer);
    
    Optional<Customer> findById(Long id);
    
    Optional<Customer> findByEmail(String email);
    
    List<Customer> findAll();
    
    boolean existsByEmail(String email);
    
    void delete(Customer customer);
    
    boolean existsById(Long id);
    
    long count();
}