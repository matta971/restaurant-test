package com.restaurant.service.restaurant.domain.port.out;

import java.util.Map;

/**
 * Port for sending notifications to external systems or users
 * This could be implemented by email, SMS, push notification adapters
 */
public interface NotificationPort {

    /**
     * Sends a notification
     * 
     * @param notification the notification to send
     */
    void sendNotification(Notification notification);

    /**
     * Base interface for notifications
     */
    interface Notification {
        String getType();
        String getRecipient();
        String getTitle();
        String getContent();
        Map<String, Object> getMetadata();
    }

    /**
     * Notification for restaurant capacity alerts
     */
    record CapacityAlertNotification(
        String recipient,
        Long restaurantId,
        String restaurantName,
        double utilizationRate,
        Map<String, Object> metadata
    ) implements Notification {
        @Override
        public String getType() {
            return "CAPACITY_ALERT";
        }

        @Override
        public String getTitle() {
            return "Restaurant Capacity Alert";
        }

        @Override
        public String getContent() {
            return String.format("Restaurant %s has reached %.1f%% capacity utilization", 
                restaurantName, utilizationRate * 100);
        }

        @Override
        public String getRecipient() {
            return recipient;
        }

        @Override
        public Map<String, Object> getMetadata() {
            return metadata != null ? metadata : Map.of();
        }
    }

    /**
     * Notification for reservation confirmations
     */
    record ReservationConfirmationNotification(
        String recipient,
        Long restaurantId,
        String restaurantName,
        java.time.LocalDate date,
        java.time.LocalTime startTime,
        java.time.LocalTime endTime,
        Integer partySize,
        String tableNumber,
        Map<String, Object> metadata
    ) implements Notification {
        @Override
        public String getType() {
            return "RESERVATION_CONFIRMATION";
        }

        @Override
        public String getTitle() {
            return "Reservation Confirmed";
        }

        @Override
        public String getContent() {
            return String.format("Your reservation at %s for %d people on %s from %s to %s (Table %s) has been confirmed", 
                restaurantName, partySize, date, startTime, endTime, tableNumber);
        }

        @Override
        public String getRecipient() {
            return recipient;
        }

        @Override
        public Map<String, Object> getMetadata() {
            return metadata != null ? metadata : Map.of();
        }
    }
}