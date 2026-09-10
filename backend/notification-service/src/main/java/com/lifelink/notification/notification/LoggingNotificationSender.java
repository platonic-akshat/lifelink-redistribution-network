package com.lifelink.notification.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Starter-repo implementation of {@link NotificationSender} that just logs.
 * This is the swap point for a real channel (Twilio for SMS, SES for email,
 * FCM for push) in production — everything upstream of this class is already
 * channel-agnostic.
 */
@Component
public class LoggingNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingNotificationSender.class);

    @Override
    public void send(UUID recipientId, String subject, String body) {
        log.info("[NOTIFY] to={} subject={} body={}", recipientId, subject, body);
    }
}
