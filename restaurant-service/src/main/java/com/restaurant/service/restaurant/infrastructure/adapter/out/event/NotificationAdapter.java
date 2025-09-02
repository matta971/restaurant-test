package com.restaurant.service.restaurant.infrastructure.adapter.out.event;

import com.restaurant.service.restaurant.domain.port.out.NotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Simple logging-based implementation of NotificationPort
 * In a production system, this would be replaced with actual notification providers:
 * - Email service (SendGrid, AWS SES, etc.)
 * - SMS service (Twilio, AWS SNS, etc.)
 * - Push notification service (Firebase, AWS SNS, etc.)
 * - Slack/Teams integration
 */
@Component
public class NotificationAdapter implements NotificationPort {

    private static final Logger logger = LoggerFactory.getLogger(NotificationAdapter.class);

    @Override
    public void sendNotification(Notification notification) {
        if (notification == null) {
            logger.warn("Attempted to send null notification");
            return;
        }

        try {
            logger.info("Sending {} notification to {}: {}", 
                       notification.getType(), 
                       notification.getRecipient(), 
                       notification.getTitle());
            
            // Log the notification content for now
            // In production, this would route to appropriate notification service
            logNotification(notification);
            
            // Simulate processing time
            simulateNotificationProcessing(notification);
            
            logger.debug("Successfully sent notification: {}", notification.getType());
            
        } catch (Exception e) {
            logger.error("Failed to send notification: {} to {}", 
                        notification.getType(), notification.getRecipient(), e);
            
            // In production, you might want to:
            // - Retry failed notifications
            // - Store in dead letter queue
            // - Alert monitoring systems
            throw new NotificationException("Failed to send notification", e);
        }
    }

    private void logNotification(Notification notification) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("\n=== NOTIFICATION ===\n");
        logMessage.append("Type: ").append(notification.getType()).append("\n");
        logMessage.append("To: ").append(notification.getRecipient()).append("\n");
        logMessage.append("Title: ").append(notification.getTitle()).append("\n");
        logMessage.append("Content: ").append(notification.getContent()).append("\n");
        
        if (!notification.getMetadata().isEmpty()) {
            logMessage.append("Metadata: ").append(notification.getMetadata()).append("\n");
        }
        
        logMessage.append("==================\n");
        
        logger.info(logMessage.toString());
    }

    private void simulateNotificationProcessing(Notification notification) {
        // Simulate different processing times based on notification type
        try {
            switch (notification.getType()) {
                case "CAPACITY_ALERT" -> Thread.sleep(100); // Fast alerts
                case "RESERVATION_CONFIRMATION" -> Thread.sleep(200); // Moderate confirmations
                default -> Thread.sleep(50); // Default
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Notification processing interrupted", e);
        }
    }

    /**
     * Exception thrown when notification sending fails
     */
    public static class NotificationException extends RuntimeException {
        public NotificationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}