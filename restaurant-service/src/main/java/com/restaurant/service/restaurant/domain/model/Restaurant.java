package com.restaurant.service.restaurant.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Restaurant domain entity representing a restaurant with its properties and business rules
 */
@Entity
@Table(name = "restaurants")
@Getter
@Setter
public class Restaurant {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    @NotBlank(message = "Restaurant name cannot be null or empty")
    private String name;

    @Column(name = "address", nullable = false)
    @NotBlank(message = "Restaurant address cannot be null or empty")
    private String address;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "email")
    @Email(message = "Invalid email format")
    private String email;

    @Column(name = "capacity", nullable = false)
    @Positive(message = "Restaurant capacity must be positive")
    private Integer capacity;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "opening_time", nullable = false)
    private LocalTime openingTime = LocalTime.of(11, 0); // Default 11:00

    @Column(name = "closing_time", nullable = false)
    private LocalTime closingTime = LocalTime.of(23, 59); // Default 23:59

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RestaurantTable> tables = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    // Default constructor for JPA
    protected Restaurant() {
    }

    /**
     * Creates a new Restaurant with the specified properties
     *
     * @param name the restaurant name (required, non-blank)
     * @param address the restaurant address (required, non-blank)
     * @param phoneNumber the phone number (optional)
     * @param email the email address (optional, must be valid format if provided)
     * @param capacity the maximum capacity (required, must be positive)
     * @throws IllegalArgumentException if any validation fails
     */
    public Restaurant(String name, String address, String phoneNumber, String email, Integer capacity) {
        this(name, address, phoneNumber, email, capacity, LocalTime.of(11, 0), LocalTime.of(23, 59));
    }

    /**
     * Creates a new Restaurant with the specified properties and opening hours
     *
     * @param name the restaurant name (required, non-blank)
     * @param address the restaurant address (required, non-blank)
     * @param phoneNumber the phone number (optional)
     * @param email the email address (optional, must be valid format if provided)
     * @param capacity the maximum capacity (required, must be positive)
     * @param openingTime the opening time (required)
     * @param closingTime the closing time (required, must be after opening time)
     * @throws IllegalArgumentException if any validation fails
     */
    public Restaurant(String name, String address, String phoneNumber, String email, Integer capacity,
                      LocalTime openingTime, LocalTime closingTime) {
        validateName(name);
        validateAddress(address);
        validateEmail(email);
        validateCapacity(capacity);
        validateOpeningHours(openingTime, closingTime);

        this.name = name.trim();
        this.address = address.trim();
        this.phoneNumber = phoneNumber != null ? phoneNumber.trim() : null;
        this.email = email != null ? email.trim() : null;
        this.capacity = capacity;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
        this.active = true;
        this.tables = new ArrayList<>();
    }

    // Business Operations

    /**
     * Activates the restaurant
     */
    public void activate() {
        this.active = true;
    }

    /**
     * Deactivates the restaurant
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * Adds a table to the restaurant
     *
     * @param table the table to add (must not be null)
     * @throws IllegalArgumentException if table is null
     */
    public void addTable(RestaurantTable table) {
        if (table == null) {
            throw new IllegalArgumentException("Table cannot be null");
        }

        if (!tables.contains(table)) {
            tables.add(table);
            table.setRestaurant(this);
        }
    }

    /**
     * Removes a table from the restaurant
     *
     * @param table the table to remove
     */
    public void removeTable(RestaurantTable table) {
        if (table != null && tables.contains(table)) {
            tables.remove(table);
            table.setRestaurant(null);
        }
    }

    /**
     * Calculates the total number of available seats across all tables
     *
     * @return total available seats
     */
    public int getTotalAvailableSeats() {
        return tables.stream()
                .filter(RestaurantTable::isAvailable)
                .mapToInt(RestaurantTable::getSeats)
                .sum();
    }

    // Validation methods

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Restaurant name cannot be null or empty");
        }
    }

    private void validateAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Restaurant address cannot be null or empty");
        }
    }

    private void validateEmail(String email) {
        if (email != null && !email.trim().isEmpty() && !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    private void validateCapacity(Integer capacity) {
        if (capacity == null || capacity <= 0) {
            throw new IllegalArgumentException("Restaurant capacity must be positive");
        }
    }

    private void validateOpeningHours(LocalTime openingTime, LocalTime closingTime) {
        if (openingTime == null) {
            throw new IllegalArgumentException("Opening time cannot be null");
        }
        if (closingTime == null) {
            throw new IllegalArgumentException("Closing time cannot be null");
        }
        if (openingTime.isBefore(LocalTime.of(5, 0)) || openingTime.isAfter(LocalTime.of(23, 30))) {
            throw new IllegalArgumentException("Opening time should be between 05:00 and 23:30");
        }
        if (closingTime.equals(openingTime)) {
            throw new IllegalArgumentException("Closing time cannot be the same as opening time");
        }
    }

    // Getters and Setters

    public void setName(String name) {
        validateName(name);
        this.name = name.trim();
    }

    public void setAddress(String address) {
        validateAddress(address);
        this.address = address.trim();
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber != null ? phoneNumber.trim() : null;
    }

    public void setEmail(String email) {
        validateEmail(email);
        this.email = email != null ? email.trim() : null;
    }

    public void setCapacity(Integer capacity) {
        validateCapacity(capacity);
        this.capacity = capacity;
    }

    public Boolean isActive() {
        return active;
    }

    public List<RestaurantTable> getTables() {
        return new ArrayList<>(tables); // Return defensive copy
    }

    public void setOpeningTime(LocalTime openingTime) {
        validateOpeningHours(openingTime, this.closingTime);
        this.openingTime = openingTime;
    }

    public void setClosingTime(LocalTime closingTime) {
        validateOpeningHours(this.openingTime, closingTime);
        this.closingTime = closingTime;
    }

    /**
     * Checks if the given time range is within restaurant opening hours
     *
     * @param startTime the start time to check
     * @param endTime the end time to check
     * @return true if within opening hours, false otherwise
     */
    public boolean isWithinOpeningHours(LocalTime startTime, LocalTime endTime) {
        // Si closingTime < openingTime, le restaurant ferme le lendemain
        if (closingTime.isBefore(openingTime)) {
            // Restaurant ouvert toute la nuit (ex: 18:00 - 01:00)
            // L'horaire est valide si :
            // - startTime >= openingTime (après l'ouverture le jour même)
            // - OU endTime <= closingTime (avant la fermeture le lendemain)
            return (startTime.compareTo(openingTime) >= 0) ||
                    (endTime.compareTo(closingTime) <= 0);
        } else {
            // Restaurant avec horaires normaux (ex: 11:00 - 23:00)
            return startTime.compareTo(openingTime) >= 0 &&
                    endTime.compareTo(closingTime) <= 0;
        }
    }

    // Equals and HashCode

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Restaurant that = (Restaurant) o;

        // If both have IDs, compare by ID
        if (id != null && that.id != null) {
            return Objects.equals(id, that.id);
        }

        // If no IDs, compare by business key (name + address)
        return Objects.equals(name, that.name) &&
                Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(name, address);
    }

    @Override
    public String toString() {
        return "Restaurant{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", capacity=" + capacity +
                ", active=" + active +
                ", tablesCount=" + tables.size() +
                '}';
    }
}