package com.btg.fondos.infrastructure.notification;

import com.btg.fondos.application.notification.NotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("smsSender")
public class FakeSmsSender implements NotificationSender {
    private static final Logger log = LoggerFactory.getLogger(FakeSmsSender.class);

    @Override
    public void send(String to, String subject, String content) {
        log.info("[FAKE SMS] to={}, content={}", to, content);
    }
}
