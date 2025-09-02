package com.restaurant.service.restaurant.domain.model;

import lombok.Getter;

/**
 * Enum representing different states of a time slot reservation
 * Defines the lifecycle and allowed transitions for reservations
 */
public enum TimeSlotStatus {
    AVAILABLE("Réservation disponible", true, true, false, false),
    CONFIRMED("Réservation confirmée", true, false, true, false),
    COMPLETED("Service terminé", false, false, false, true),
    CANCELLED("Réservation annulée", false, false, false, true);

    /**
     * -- GETTER --
     *  Get human-readable description of the status
     *
     */
    @Getter
    private final String description;
    /**
     * -- GETTER --
     *  Check if this status represents an active reservation
     *
     */
    @Getter
    private final boolean active;
    private final boolean allowsConfirmation;
    private final boolean allowsCancellation;
    private final boolean finalStatus;

    TimeSlotStatus(String description, boolean active, boolean allowsConfirmation,
                   boolean allowsCancellation, boolean finalStatus) {
        this.description = description;
        this.active = active;
        this.allowsConfirmation = allowsConfirmation;
        this.allowsCancellation = allowsCancellation;
        this.finalStatus = finalStatus;
    }

    /**
     * Check if confirmation is allowed from this status
     * @return true if confirmation allowed, false otherwise
     */
    public boolean allowsConfirmation() {
        return allowsConfirmation;
    }

    /**
     * Check if cancellation is allowed from this status
     * @return true if cancellation allowed, false otherwise
     */
    public boolean allowsCancellation() {
        return allowsCancellation;
    }

    /**
     * Check if this is a final status (no further transitions allowed)
     * @return true if final, false otherwise
     */
    public boolean isFinal() {
        return finalStatus;
    }
}