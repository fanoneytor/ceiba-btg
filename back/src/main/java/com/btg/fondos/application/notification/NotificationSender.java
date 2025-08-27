package com.btg.fondos.application.notification;

public interface NotificationSender {
    void send(String to, String subject, String content);
}
