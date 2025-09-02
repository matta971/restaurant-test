package com.restaurant.service.restaurant.domain.model;

import lombok.Getter;

/**
 * Enum representing different table locations in a restaurant
 * Each location has specific business properties and constraints
 */
public enum TableLocation {
    WINDOW("Table fenêtre", false, false, false),
    TERRACE("Table exterieur terasse", true, false, true),
    INDOOR("Table en intérieur", false, false, false),
    PRIVATE_ROOM("Table isolée", false, true, false);

    /**
     * -- GETTER --
     *  Get human-readable description of the table location
     *
     */
    @Getter
    private final String description;
    /**
     * -- GETTER --
     *  Check if this location is outdoor
     *
     */
    @Getter
    private final boolean outdoor;
    private final boolean requiresReservation;
    /**
     * -- GETTER --
     *  Check if this location is weather dependent
     *
     */
    @Getter
    private final boolean weatherDependent;

    TableLocation(String description, boolean outdoor, boolean requiresReservation, boolean weatherDependent) {
        this.description = description;
        this.outdoor = outdoor;
        this.requiresReservation = requiresReservation;
        this.weatherDependent = weatherDependent;
    }

    /**
     * Check if this location requires advance reservation
     * @return true if reservation required, false otherwise
     */
    public boolean requiresReservation() {
        return requiresReservation;
    }

}