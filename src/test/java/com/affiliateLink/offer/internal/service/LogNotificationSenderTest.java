package com.affiliateLink.offer.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import com.affiliateLink.offer.internal.service.impliments.LogNotificationSender;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

class LogNotificationSenderTest {

    private final LogNotificationSender sender = new LogNotificationSender();

    private Logger logger;
    private ListAppender<ILoggingEvent> logs;
    private Level previousLevel;

    @BeforeEach
    void captureLogs() {
        logger = (Logger) LoggerFactory.getLogger(LogNotificationSender.class);
        previousLevel = logger.getLevel();
        logger.setLevel(Level.DEBUG);          // باش نشوفو حتى سطر الـ DEBUG

        logs = new ListAppender<>();
        logs.start();
        logger.addAppender(logs);
    }

    @AfterEach
    void releaseLogs() {
        logger.detachAppender(logs);
        logger.setLevel(previousLevel);
    }

    @Test
    void sendLogsOneInfoLineWithTheNumberOfRecipients() {
        // Given
        List<String> recipients = List.of("p1@example.com", "p2@example.com");

        // When
        sender.send(recipients, "Stop sharing links of Acme", "Please stop sharing.");

        // Then
        List<ILoggingEvent> infoLines = logs.list.stream()
                .filter(event -> event.getLevel() == Level.INFO)
                .toList();

        assertThat(infoLines).hasSize(1);
        assertThat(infoLines.get(0).getFormattedMessage())
                .contains("Stop sharing links of Acme")
                .contains("2 publisher(s)");
    }

    @Test
    void sendNeverWritesRecipientEmailsInTheLogs() {
        // Given
        List<String> recipients = List.of("p1@example.com", "p2@example.com");

        // When
        sender.send(recipients, "Subject", "Body");

        // Then: aucune donnée personnelle dans les logs
        assertThat(logs.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .noneMatch(message -> message.contains("@"));
    }

    @Test
    void sendWritesTheMessageBodyOnlyAtDebugLevel() {
        // When
        sender.send(List.of("p1@example.com"), "Subject", "Please stop sharing.");

        // Then
        List<ILoggingEvent> bodyLines = logs.list.stream()
                .filter(event -> event.getFormattedMessage().contains("Please stop sharing."))
                .toList();

        assertThat(bodyLines).hasSize(1);
        assertThat(bodyLines.get(0).getLevel()).isEqualTo(Level.DEBUG);
    }

    @Test
    void sendWithNoRecipientDoesNotFail() {
        assertThatCode(() -> sender.send(List.of(), "Subject", "Body"))
                .doesNotThrowAnyException();

        assertThat(logs.list.get(0).getFormattedMessage()).contains("0 publisher(s)");
    }
}