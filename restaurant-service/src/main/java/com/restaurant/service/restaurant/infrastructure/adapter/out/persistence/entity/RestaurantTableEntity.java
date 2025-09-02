package com.restaurant.service.restaurant.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Entity for RestaurantTable
 * Maps to restaurant_table table in database
 */
@Entity
@Table(name = "restaurant_table", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"restaurant_id", "table_number"}))
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantTableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private RestaurantEntity restaurant;

    @Column(name = "seats", nullable = false)
    private Integer seats;

    @Enumerated(EnumType.STRING)
    @Column(name = "location", nullable = false)
    private TableLocationEntity location;

    @Column(name = "available", nullable = false)
    private Boolean available = true;

    @Column(name = "table_number")
    private Integer tableNumber;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version = 0L;

    @OneToMany(mappedBy = "table", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TimeSlotEntity> timeSlots = new ArrayList<>();

    public RestaurantTableEntity(Integer seats, TableLocationEntity location) {
        this.seats = seats;
        this.location = location;
        this.available = true;
    }

    public RestaurantTableEntity(Integer seats, TableLocationEntity location, Integer tableNumber) {
        this.seats = seats;
        this.location = location;
        this.tableNumber = tableNumber;
        this.available = true;
    }

    public void addTimeSlot(TimeSlotEntity timeSlot) {
        timeSlots.add(timeSlot);
        timeSlot.setTable(this);
    }

    public void removeTimeSlot(TimeSlotEntity timeSlot) {
        timeSlots.remove(timeSlot);
        timeSlot.setTable(null);
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (available == null) {
            available = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Enum for table locations in JPA entity
     */
    public enum TableLocationEntity {
        WINDOW, TERRACE, INDOOR, PRIVATE_ROOM
    }
}