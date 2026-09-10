package com.lifelink.notification.notification;

import java.util.UUID;

/**
 * Abstraction over "deliver a notification to somebody". {@code recipientId}
 * is intentionally opaque here (a needId, providerId, etc.) — in production
 * this would be resolved to an email address / phone number / device token
 * via a user-directory lookup before being handed to a real channel.
 */
public interface NotificationSender {

    void send(UUID recipientId, String subject, String body);
}
