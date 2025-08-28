package com.btg.fondos.presentation.mapper;

import com.btg.fondos.domain.enums.NotificationChannel;
import com.btg.fondos.domain.model.Client;
import com.btg.fondos.domain.model.Fund;
import com.btg.fondos.domain.model.Transaction;
import com.btg.fondos.presentation.dto.ActiveSubscriptionResponse;
import com.btg.fondos.presentation.dto.ClientCreateRequest; // Import ClientCreateRequest
import com.btg.fondos.presentation.dto.ClientResponse;
import com.btg.fondos.presentation.dto.FundResponse;
import com.btg.fondos.presentation.dto.TransactionResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DtoMapper {

    public static TransactionResponse toResponse(Transaction t) {
        return new TransactionResponse(
                t.getId(), t.getTransactionId(), t.getClientId(), t.getFundId(),
                t.getType(), t.getAmount(), t.getStatus(), t.getMessage(), t.getDate()
        );
    }

    public static FundResponse toResponse(Fund f) {
        return new FundResponse(
                f.getId(), f.getName(), f.getMinimumAmount(), f.getCategory()
        );
    }

    public static ClientResponse toResponse(Client c, List<ActiveSubscriptionResponse> activeSubs) {
        return new ClientResponse(
                c.getId(),
                c.getName(),
                c.getEmail(),
                c.getPhone(),
                c.getAvailableBalance(),
                c.getPreferredNotification(),
                activeSubs,
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }

    public static Client toClient(ClientCreateRequest req) {
        var c = new Client();
        c.setName(req.name());
        c.setEmail(req.email());
        c.setPhone(req.phone());
        c.setAvailableBalance(Optional.ofNullable(req.initialBalance()).orElse(new BigDecimal("500000")));
        c.setPreferredNotification(Optional.ofNullable(req.preferredNotification()).orElse(NotificationChannel.EMAIL));
        c.setActiveFunds(new ArrayList<>());
        c.setCreatedAt(Instant.now());
        c.setUpdatedAt(Instant.now());
        return c;
    }
}