package com.restaurant.service.restaurant.infrastructure.adapter.out.notification;

import com.restaurant.service.restaurant.domain.port.out.NotificationPort;
import org.springframework.stereotype.Component;

/**
 * Implementation of NotificationPort for development
 * In production, this would integrate with email service, SMS service, etc.
 */
@Component
public class NotificationService implements NotificationPort {

    @Override
    public void sendNotification(Notification notification) {
        // For now, just log the notification
        System.out.println("Sending " + notification.getType() + " notification:");
        System.out.println("To: " + notification.getRecipient());
        System.out.println("Title: " + notification.getTitle());
        System.out.println("Content: " + notification.getContent());
        System.out.println("Metadata: " + notification.getMetadata());
        
        // In a real implementation, you would:
        // - Choose the appropriate channel (email, SMS, push)
        // - Use proper templates
        // - Handle delivery failures
        // - Track delivery status
        // - Implement rate limiting
    }
}