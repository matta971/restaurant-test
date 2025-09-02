package com.restaurant.service.restaurant.domain.model;

import jakarta.persistence.Embeddable;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Value Object representing a time range with business validation
 * Encapsulates time range logic and constraints
 */
@Embeddable
public class TimeRange {
    
    private static final int MIN_DURATION_MINUTES = 30;
    private static final int MAX_DURATION_MINUTES = 240; // 4 hours

    private final LocalTime startTime;
    private final LocalTime endTime;

    // Default constructor for JPA
    protected TimeRange() {
        this.startTime = null;
        this.endTime = null;
    }

    /**
     * Creates a time range value object
     * 
     * @param startTime the start time (required)
     * @param endTime the end time (required, must be after start)
     * @throws IllegalArgumentException if validation fails
     */
    public TimeRange(LocalTime startTime, LocalTime endTime) {
        validateTimes(startTime, endTime);
        validateDuration(startTime, endTime);

        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * Creates a time range from start time and duration in minutes
     * 
     * @param startTime the start time
     * @param durationMinutes duration in minutes
     * @return new TimeRange instance
     */
    public static TimeRange ofDuration(LocalTime startTime, int durationMinutes) {
        if (startTime == null) {
            throw new IllegalArgumentException("Start time cannot be null");
        }
        if (durationMinutes < MIN_DURATION_MINUTES || durationMinutes > MAX_DURATION_MINUTES) {
            throw new IllegalArgumentException(
                "Duration must be between " + MIN_DURATION_MINUTES + " and " + MAX_DURATION_MINUTES + " minutes");
        }

        LocalTime endTime = startTime.plusMinutes(durationMinutes);
        return new TimeRange(startTime, endTime);
    }

    /**
     * Creates a time range from two time strings (HH:mm format)
     * 
     * @param startTimeStr start time as string
     * @param endTimeStr end time as string
     * @return new TimeRange instance
     */
    public static TimeRange of(String startTimeStr, String endTimeStr) {
        try {
            LocalTime start = LocalTime.parse(startTimeStr);
            LocalTime end = LocalTime.parse(endTimeStr);
            return new TimeRange(start, end);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid time format. Use HH:mm format", e);
        }
    }

    /**
     * Checks if this time range overlaps with another
     * 
     * @param other the other time range
     * @return true if they overlap, false otherwise
     */
    public boolean overlapsWith(TimeRange other) {
        if (other == null) {
            return false;
        }
        
        // Two ranges overlap if: start1 < end2 AND start2 < end1
        return startTime.isBefore(other.endTime) && other.startTime.isBefore(endTime);
    }

    /**
     * Checks if this time range contains the given time
     * 
     * @param time the time to check
     * @return true if time is within this range, false otherwise
     */
    public boolean contains(LocalTime time) {
        if (time == null) {
            return false;
        }
        return !time.isBefore(startTime) && !time.isAfter(endTime);
    }

    /**
     * Calculates the duration of this time range in minutes
     * 
     * @return duration in minutes
     */
    public long getDurationInMinutes() {
        return ChronoUnit.MINUTES.between(startTime, endTime);
    }

    /**
     * Calculates the duration in hours and minutes as a formatted string
     * 
     * @return duration as "X hours Y minutes" format
     */
    public String getFormattedDuration() {
        long totalMinutes = getDurationInMinutes();
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        
        if (hours == 0) {
            return minutes + " minutes";
        } else if (minutes == 0) {
            return hours + (hours == 1 ? " hour" : " hours");
        } else {
            return hours + (hours == 1 ? " hour " : " hours ") + 
                   minutes + (minutes == 1 ? " minute" : " minutes");
        }
    }

    /**
     * Checks if this time range is within business hours
     * 
     * @param businessStart business start time
     * @param businessEnd business end time
     * @return true if within business hours, false otherwise
     */
    public boolean isWithinBusinessHours(LocalTime businessStart, LocalTime businessEnd) {
        return !startTime.isBefore(businessStart) && !endTime.isAfter(businessEnd);
    }

    private void validateTimes(LocalTime startTime, LocalTime endTime) {
        if (startTime == null) {
            throw new IllegalArgumentException("Start time cannot be null");
        }
        if (endTime == null) {
            throw new IllegalArgumentException("End time cannot be null");
        }
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
    }

    private void validateDuration(LocalTime startTime, LocalTime endTime) {
        long durationMinutes = ChronoUnit.MINUTES.between(startTime, endTime);
        
        if (durationMinutes < MIN_DURATION_MINUTES) {
            throw new IllegalArgumentException("Duration must be at least " + MIN_DURATION_MINUTES + " minutes");
        }
        
        if (durationMinutes > MAX_DURATION_MINUTES) {
            throw new IllegalArgumentException("Duration cannot exceed " + MAX_DURATION_MINUTES + " minutes");
        }
    }

    // Getters

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    // Value Object equality

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeRange timeRange = (TimeRange) o;
        return Objects.equals(startTime, timeRange.startTime) &&
               Objects.equals(endTime, timeRange.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startTime, endTime);
    }

    @Override
    public String toString() {
        return "TimeRange{" +
                "startTime=" + startTime +
                ", endTime=" + endTime +
                ", duration=" + getFormattedDuration() +
                '}';
    }
}