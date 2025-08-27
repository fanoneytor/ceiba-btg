package com.btg.fondos.domain.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class SubscriptionCreatedEvent extends ApplicationEvent {
    private final String clientId;
    private final String fundId;
    private final String transactionId;

    public SubscriptionCreatedEvent(Object source, String clientId, String fundId, String transactionId) {
        super(source);
        this.clientId = clientId;
        this.fundId = fundId;
        this.transactionId = transactionId;
    }

}
