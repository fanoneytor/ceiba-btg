package com.btg.fondos.application.notification;

import com.btg.fondos.domain.enums.NotificationChannel;
import com.btg.fondos.domain.events.SubscriptionCreatedEvent;
import com.btg.fondos.domain.model.Client;
import com.btg.fondos.domain.model.Fund;
import com.btg.fondos.infrastructure.repository.ClientRepository;
import com.btg.fondos.infrastructure.repository.FundRepository;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionNotificationListener {

    private final ClientRepository clientRepo;
    private final FundRepository fundRepo;
    private final NotificationService notifier;

    public SubscriptionNotificationListener(ClientRepository clientRepo, FundRepository fundRepo, NotificationService notifier) {
        this.clientRepo = clientRepo;
        this.fundRepo = fundRepo;
        this.notifier = notifier;
    }

    @Async
    @EventListener
    public void onSubscriptionCreated(SubscriptionCreatedEvent event) {
        Client c = clientRepo.findById(event.getClientId()).orElse(null);
        Fund f = fundRepo.findById(event.getFundId()).orElse(null);
        if (c == null || f == null) return;

        String subject = "Subscription successful";
        String content = "You subscribed to fund " + f.getName() + " (Tx: " + event.getTransactionId() + ").";

        NotificationChannel ch = c.getPreferredNotification();
        if (ch == NotificationChannel.EMAIL && c.getEmail() != null)
            notifier.notify(ch, c.getEmail(), subject, content);
        else if (ch == NotificationChannel.SMS && c.getPhone() != null)
            notifier.notify(ch, c.getPhone(), subject, content);
    }
}
