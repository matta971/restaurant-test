package com.restaurant.service.restaurant.infrastructure.adapter.out.notification;

import com.restaurant.service.restaurant.domain.port.out.NotificationPort;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Adapter for sending notifications - currently a mock implementation for development
 * In production, this could be replaced with email, SMS, or push notification services
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Primary
public class MockNotificationAdapter implements NotificationPort {

    @Override
    public void sendNotification(Notification notification) {
        log.info("Sending {} notification to {}: {}", 
                notification.getType(), 
                notification.getRecipient(), 
                notification.getTitle());
        
        // Simulate async notification sending
        CompletableFuture.runAsync(() -> {
            try {
                // Simulate network delay
                Thread.sleep(200);
                log.info("✓ Notification sent successfully: {}", notification.getTitle());
                log.debug("Notification content: {}", notification.getContent());
                
                // Log metadata if present
                if (!notification.getMetadata().isEmpty()) {
                    log.debug("Notification metadata: {}", notification.getMetadata());
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("✗ Notification sending interrupted: {}", notification.getTitle(), e);
            } catch (Exception e) {
                log.error("✗ Failed to send notification: {}", notification.getTitle(), e);
            }
        }).exceptionally(throwable -> {
            log.error("✗ Async notification failed: {}", notification.getTitle(), throwable);
            return null;
        });
    }
}

/**
 * Email notification adapter for production use
 * This would be enabled when email properties are configured
 */
@Component
@ConditionalOnProperty(prefix = "app.notification.email", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
class EmailNotificationAdapter implements NotificationPort {

    // In a real implementation, you would inject:
    // private final JavaMailSender mailSender;
    // private final TemplateEngine templateEngine;

    @Override
    public void sendNotification(Notification notification) {
        log.info("Sending email notification to {}: {}", 
                notification.getRecipient(), 
                notification.getTitle());
        
        try {
            // Real implementation would:
            // 1. Load email template based on notification type
            // 2. Populate template with notification data
            // 3. Send email via JavaMailSender
            // 4. Handle bounces and failures
            
            simulateEmailSending(notification);
            
        } catch (Exception e) {
            log.error("Failed to send email notification", e);
            throw new NotificationException("Email sending failed", e);
        }
    }

    private void simulateEmailSending(Notification notification) {
        // Simulate email template rendering and sending
        String emailTemplate = generateEmailTemplate(notification);
        
        log.info("Email template generated for {}", notification.getType());
        log.debug("Email content preview: {}", 
                emailTemplate.substring(0, Math.min(100, emailTemplate.length())));
        
        // Simulate successful sending
        log.info("✓ Email sent successfully to {}", notification.getRecipient());
    }

    private String generateEmailTemplate(Notification notification) {
        if (notification instanceof NotificationPort.CapacityAlertNotification alert) {
            return generateCapacityAlertEmail(alert);
        } else if (notification instanceof NotificationPort.ReservationConfirmationNotification reservation) {
            return generateReservationConfirmationEmail(reservation);
        } else {
            return generateGenericEmail(notification);
        }
    }

    private String generateCapacityAlertEmail(NotificationPort.CapacityAlertNotification alert) {
        return String.format("""
                <!DOCTYPE html>
                <html>
                <head><title>Capacity Alert</title></head>
                <body>
                    <h2>Capacity Alert for %s</h2>
                    <p>Your restaurant has reached <strong>%.1f%%</strong> capacity utilization.</p>
                    <p>Consider:</p>
                    <ul>
                        <li>Adjusting table turnover times</li>
                        <li>Managing walk-in availability</li>
                        <li>Coordinating with kitchen staff</li>
                    </ul>
                    <p>Best regards,<br/>Restaurant Management System</p>
                </body>
                </html>
                """, 
                alert.restaurantName(), 
                alert.utilizationRate() * 100);
    }

    private String generateReservationConfirmationEmail(NotificationPort.ReservationConfirmationNotification reservation) {
        return String.format("""
                <!DOCTYPE html>
                <html>
                <head><title>Reservation Confirmed</title></head>
                <body>
                    <h2>🎉 Reservation Confirmed!</h2>
                    <div style="border: 1px solid #ccc; padding: 15px; margin: 10px 0;">
                        <h3>%s</h3>
                        <p><strong>Date:</strong> %s</p>
                        <p><strong>Time:</strong> %s - %s</p>
                        <p><strong>Party Size:</strong> %d people</p>
                        <p><strong>Table:</strong> %s</p>
                    </div>
                    <p>We look forward to serving you!</p>
                    <p>If you need to modify or cancel your reservation, please contact us.</p>
                    <p>Best regards,<br/>%s Team</p>
                </body>
                </html>
                """, 
                reservation.restaurantName(),
                reservation.date(),
                reservation.startTime(),
                reservation.endTime(),
                reservation.partySize(),
                reservation.tableNumber(),
                reservation.restaurantName());
    }

    private String generateGenericEmail(Notification notification) {
        return String.format("""
                <!DOCTYPE html>
                <html>
                <head><title>%s</title></head>
                <body>
                    <h2>%s</h2>
                    <p>%s</p>
                    <hr>
                    <small>Sent by Restaurant Management System</small>
                </body>
                </html>
                """, 
                notification.getTitle(),
                notification.getTitle(), 
                notification.getContent());
    }

    public static class NotificationException extends RuntimeException {
        public NotificationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

/**
 * SMS notification adapter for production use
 */
@Component
@ConditionalOnProperty(prefix = "app.notification.sms", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
class SmsNotificationAdapter implements NotificationPort {

    @Override
    public void sendNotification(Notification notification) {
        log.info("Sending SMS notification to {}: {}", 
                notification.getRecipient(), 
                notification.getTitle());
        
        // Real implementation would integrate with SMS provider:
        // - Twilio, AWS SNS, or similar service
        // - Format message for SMS constraints
        // - Handle delivery receipts
        
        String smsContent = formatForSms(notification);
        log.info("SMS content: {}", smsContent);
        log.info("✓ SMS sent successfully to {}", notification.getRecipient());
    }

    private String formatForSms(Notification notification) {
        if (notification instanceof NotificationPort.ReservationConfirmationNotification reservation) {
            return String.format(
                    "Reservation confirmed at %s for %d on %s at %s. Table %s",
                    reservation.restaurantName(),
                    reservation.partySize(),
                    reservation.date(),
                    reservation.startTime(),
                    reservation.tableNumber()
            );
        } else {
            return String.format("%s: %s", notification.getTitle(), notification.getContent());
        }
    }

}

/**
 * Configuration for notification services
 */
@Configuration
@ConfigurationProperties(prefix = "app.notification")
@Data
class NotificationConfiguration {
    
    private Email email = new Email();
    private Sms sms = new Sms();
    
    @Data
    public static class Email {
        private boolean enabled = false;
        private String from = "noreply@restaurant-system.com";
        private String replyTo = "support@restaurant-system.com";
        private String templatePath = "classpath:/templates/email/";
    }
    
    @Data
    public static class Sms {
        private boolean enabled = false;
        private String provider = "mock";
        private String apiKey;
        private String fromNumber;
    }
}