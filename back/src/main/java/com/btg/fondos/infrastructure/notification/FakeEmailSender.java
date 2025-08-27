package com.btg.fondos.infrastructure.notification;

import com.btg.fondos.application.notification.NotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("emailSender")
public class FakeEmailSender implements NotificationSender {
    private static final Logger log = LoggerFactory.getLogger(FakeEmailSender.class);

    @Override
    public void send(String to, String subject, String content) {
        log.info("[FAKE EMAIL] to={}, subject={}, content={}", to, subject, content);
    }
}
