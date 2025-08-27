package com.btg.fondos.presentation.controller;

import com.btg.fondos.domain.enums.NotificationChannel;
import com.btg.fondos.domain.model.Client;
import com.btg.fondos.domain.model.Fund;
import com.btg.fondos.presentation.dto.ActiveSubscriptionResponse;
import com.btg.fondos.presentation.dto.ClientCreateRequest;
import com.btg.fondos.presentation.dto.ClientResponse;
import com.btg.fondos.presentation.mapper.DtoMapper;
import com.btg.fondos.application.service.ClientService;
import com.btg.fondos.application.service.FundService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@RestController
@RequestMapping("/api/clients")
@CrossOrigin
public class ClientsController {

    private final ClientService clientService;
    private final FundService fundService;

    public ClientsController(ClientService clientService, FundService fundService) {
        this.clientService = clientService;
        this.fundService = fundService;
    }

    @PostMapping
    public ResponseEntity<ClientResponse> create(@RequestBody ClientCreateRequest req) {
        Client c = new Client();
        c.setName(req.name());
        c.setEmail(req.email());
        c.setPhone(req.phone());
        c.setAvailableBalance(
                Optional.ofNullable(req.initialBalance()).orElse(new BigDecimal("500000"))
        );
        c.setPreferredNotification(
                Optional.ofNullable(req.preferredNotification()).orElse(NotificationChannel.EMAIL)
        );
        c.setActiveFunds(new java.util.ArrayList<>());
        c.setCreatedAt(Instant.now());
        c.setUpdatedAt(Instant.now());

        Client saved = clientService.save(c);

        var activeSubs = saved.getActiveFunds().stream().map(sub -> {
            var fundName = fundService.getById(sub.getFundId()).map(Fund::getName).orElse("UNKNOWN");
            return new ActiveSubscriptionResponse(
                    sub.getFundId(), fundName, sub.getInvestedAmount(), sub.getSubscriptionDate()
            );
        }).toList();

        return ResponseEntity.ok(DtoMapper.toResponse(saved, activeSubs));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> get(@PathVariable String id) {
        var client = clientService.getById(id).orElse(null);
        if (client == null) return ResponseEntity.notFound().build();

        var activeSubs = client.getActiveFunds().stream().map(sub -> {
            var fundName = fundService.getById(sub.getFundId()).map(Fund::getName).orElse("UNKNOWN");
            return new ActiveSubscriptionResponse(
                    sub.getFundId(), fundName, sub.getInvestedAmount(), sub.getSubscriptionDate()
            );
        }).toList();

        return ResponseEntity.ok(DtoMapper.toResponse(client, activeSubs));
    }
}
