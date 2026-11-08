package com.affiliateLink.offer.internal.service;

import java.util.List;

public interface NotificationSender {

    void send(List<String> recipients, String subject, String message);
}