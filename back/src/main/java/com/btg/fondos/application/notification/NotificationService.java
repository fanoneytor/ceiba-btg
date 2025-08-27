package com.btg.fondos.application.notification;

import com.btg.fondos.domain.enums.NotificationChannel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final NotificationSender emailSender;
    private final NotificationSender smsSender;

    public NotificationService(
            @Qualifier("emailSender") NotificationSender emailSender,
            @Qualifier("smsSender") NotificationSender smsSender) {
        this.emailSender = emailSender;
        this.smsSender = smsSender;
    }

    public void notify(NotificationChannel channel, String to, String subject, String content) {
        if (to == null || to.isBlank()) return;
        switch (channel) {
            case EMAIL -> emailSender.send(to, subject, content);
            case SMS -> smsSender.send(to, subject, content);
            default -> {
            }
        }
    }
}