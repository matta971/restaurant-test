package com.restaurant.service.restaurant.infrastructure.adapter.out.mock;

import com.restaurant.service.restaurant.domain.port.out.NotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Mock implementation of NotificationPort for testing and development
 * Simply logs notifications instead of actually sending them
 */
@Component
@ConditionalOnProperty(name = "restaurant.notifications.adapter", havingValue = "mock", matchIfMissing = true)
public class MockNotificationAdapter implements NotificationPort {

    private static final Logger logger = LoggerFactory.getLogger(MockNotificationAdapter.class);

    @Override
    public void sendNotification(Notification notification) {
        logger.info("Sending {} notification to: {}",
                notification.getType(),
                notification.getRecipient());

        logger.info("Subject: {}", notification.getTitle());
        logger.info("Content: {}", notification.getContent());

        if (!notification.getMetadata().isEmpty()) {
            logger.info("Metadata: {}", notification.getMetadata());
        }

        // In a real implementation, this would send via email, SMS, push notification, etc.
        // For now, we just log the notification
        logNotificationDetails(notification);
    }

    private void logNotificationDetails(Notification notification) {
        if (notification instanceof NotificationPort.CapacityAlertNotification alert) {
            logger.warn("CAPACITY ALERT: Restaurant {} has reached {}% utilization",
                    alert.restaurantName(),
                    Math.round(alert.utilizationRate() * 100));
        } else if (notification instanceof NotificationPort.ReservationConfirmationNotification confirmation) {
            logger.info("RESERVATION CONFIRMED: {} people at {} on {} from {} to {}",
                    confirmation.partySize(),
                    confirmation.restaurantName(),
                    confirmation.date(),
                    confirmation.startTime(),
                    confirmation.endTime());
        } else {
            logger.info("Unknown notification type: {}", notification.getType());
        }
    }
}