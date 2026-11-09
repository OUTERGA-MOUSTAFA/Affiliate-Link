package com.affiliateLink.offer.internal.service.impliments;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.affiliateLink.offer.internal.service.NotificationSender;

@Service 
public class LogNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(LogNotificationSender.class);

    @Override
    public void send(List<String> recipients, String subject, String message) {
        // no sensative infos in logs
        log.info("Notification '{}' sent to {} publisher(s)", subject, recipients.size());
        log.debug("Notification body: {}", message);
    }
}
