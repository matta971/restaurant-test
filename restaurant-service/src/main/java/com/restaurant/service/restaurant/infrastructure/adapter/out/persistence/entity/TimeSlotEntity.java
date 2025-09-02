package com.restaurant.service.restaurant.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * JPA Entity for TimeSlot
 * Maps to time_slot table in database
 */
@Entity
@Table(name = "time_slot", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"table_id", "date", "start_time", "end_time"}))
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false)
    private RestaurantTableEntity table;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "party_size", nullable = false)
    private Integer partySize;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TimeSlotStatusEntity status = TimeSlotStatusEntity.AVAILABLE;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "customer_phone", length = 20)
    private String customerPhone;

    @Column(name = "customer_email", length = 100)
    private String customerEmail;

    @Column(name = "special_requests", length = 1000)
    private String specialRequests;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version = 0L;

    public TimeSlotEntity(LocalDate date, LocalTime startTime, LocalTime endTime, Integer partySize) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.partySize = partySize;
        this.status = TimeSlotStatusEntity.AVAILABLE;
    }

    public TimeSlotEntity(LocalDate date, LocalTime startTime, LocalTime endTime, Integer partySize, 
                         String customerName, String customerPhone, String customerEmail) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.partySize = partySize;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.customerEmail = customerEmail;
        this.status = TimeSlotStatusEntity.RESERVED;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = TimeSlotStatusEntity.AVAILABLE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Enum for time slot status in JPA entity
     */
    public enum TimeSlotStatusEntity {
        AVAILABLE, RESERVED, CONFIRMED, CANCELLED, COMPLETED
    }
}