package com.restaurant.service.restaurant.infrastructure.adapter.out.notification;

import com.restaurant.service.restaurant.domain.port.out.NotificationPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Simple notification adapter implementation
 * This is a mock implementation that logs notifications instead of actually sending them
 * 
 * In a real-world scenario, this could be enhanced to:
 * - Send emails via SMTP
 * - Send SMS via Twilio or similar services
 * - Send push notifications
 * - Integrate with notification services like AWS SNS
 */
@Component
@Slf4j
public class NotificationAdapter implements NotificationPort {

    @Override
    public void sendNotification(Notification notification) {
        try {
            log.info("Sending {} notification to: {}", 
                    notification.getType(), notification.getRecipient());
            log.info("Title: {}", notification.getTitle());
            log.info("Content: {}", notification.getContent());
            
            if (!notification.getMetadata().isEmpty()) {
                log.debug("Metadata: {}", notification.getMetadata());
            }
            
            // Simulate notification sending
            simulateNotificationDelivery(notification);
            
            log.info("Successfully sent {} notification to: {}", 
                    notification.getType(), notification.getRecipient());
            
        } catch (Exception e) {
            log.error("Failed to send {} notification to: {}", 
                     notification.getType(), notification.getRecipient(), e);
            
            // In production, you might want to:
            // - Retry failed notifications
            // - Store in dead letter queue
            // - Alert monitoring systems
            throw new NotificationDeliveryException(
                "Failed to send notification to: " + notification.getRecipient(), e);
        }
    }
    
    private void simulateNotificationDelivery(Notification notification) {
        // Simulate different delivery mechanisms based on notification type
        switch (notification.getType()) {
            case "CAPACITY_ALERT" -> simulateEmailDelivery(notification);
            case "RESERVATION_CONFIRMATION" -> simulateSmsDelivery(notification);
            default -> log.debug("Using default delivery method for: {}", notification.getType());
        }
    }
    
    private void simulateEmailDelivery(Notification notification) {
        log.debug("📧 Simulating email delivery...");
        // In real implementation, this would use an email service
        try {
            Thread.sleep(100); // Simulate network delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.debug("✅ Email delivered successfully");
    }
    
    private void simulateSmsDelivery(Notification notification) {
        log.debug("📱 Simulating SMS delivery...");
        // In real implementation, this would use an SMS service
        try {
            Thread.sleep(50); // Simulate network delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.debug("✅ SMS delivered successfully");
    }

    /**
     * Exception thrown when notification delivery fails
     */
    public static class NotificationDeliveryException extends RuntimeException {
        public NotificationDeliveryException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}