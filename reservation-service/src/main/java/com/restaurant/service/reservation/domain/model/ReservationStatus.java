package com.restaurant.service.reservation.domain.model;

import lombok.Getter;

/**
 * Enumeration representing the different states of a reservation
 * Each status has specific business rules and allowed transitions
 */
@Getter
public enum ReservationStatus {
    
    PENDING("En attente de confirmation", "Réservation créée, en attente de confirmation"),
    CONFIRMED("Confirmée", "Réservation confirmée par le restaurant"),
    COMPLETED("Terminée", "Service terminé avec succès"),
    CANCELLED("Annulée", "Réservation annulée"),
    NO_SHOW("Absence", "Client ne s'est pas présenté");

    private final String displayName;
    private final String description;

    ReservationStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Checks if the status is active (reservation still valid)
     */
    public boolean isActive() {
        return this == PENDING || this == CONFIRMED;
    }

    /**
     * Checks if the status allows confirmation
     */
    public boolean allowsConfirmation() {
        return this == PENDING;
    }

    /**
     * Checks if the status allows cancellation
     */
    public boolean allowsCancellation() {
        return this == PENDING || this == CONFIRMED;
    }

    /**
     * Checks if the status allows modification
     */
    public boolean allowsModification() {
        return this == PENDING;
    }

    /**
     * Checks if the status is final (no more transitions allowed)
     */
    public boolean isFinal() {
        return this == COMPLETED || this == CANCELLED || this == NO_SHOW;
    }

    /**
     * Checks if the status requires customer action
     */
    public boolean requiresCustomerAction() {
        return this == PENDING;
    }

    /**
     * Checks if the status requires restaurant action
     */
    public boolean requiresRestaurantAction() {
        return this == CONFIRMED;
    }
}