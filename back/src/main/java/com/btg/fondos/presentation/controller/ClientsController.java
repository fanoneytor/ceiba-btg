package com.btg.fondos.presentation.controller;

import com.btg.fondos.domain.model.Fund;
import com.btg.fondos.presentation.dto.ActiveSubscriptionResponse;
import com.btg.fondos.presentation.dto.ClientCreateRequest;
import com.btg.fondos.presentation.dto.ClientResponse;
import com.btg.fondos.presentation.mapper.DtoMapper;
import com.btg.fondos.application.service.ClientService;
import com.btg.fondos.application.service.FundService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<ClientResponse> create(@Valid @RequestBody ClientCreateRequest req) {
        var saved = clientService.createClient(req);
        return ResponseEntity.ok(toResponseWithActiveFunds(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> get(@PathVariable String id) {
        return clientService.getById(id)
                .map(this::toResponseWithActiveFunds)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<ClientResponse>> list() {
        var clients = clientService.getAll().stream()
                .map(this::toResponseWithActiveFunds)
                .toList();
        return ResponseEntity.ok(clients);
    }

    private ClientResponse toResponseWithActiveFunds(com.btg.fondos.domain.model.Client client) {
        var activeSubs = client.getActiveFunds().stream().map(sub -> {
            var fundName = fundService.getById(sub.getFundId()).map(Fund::getName).orElse("UNKNOWN");
            return new ActiveSubscriptionResponse(
                    sub.getFundId(), fundName, sub.getInvestedAmount(), sub.getSubscriptionDate()
            );
        }).toList();
        return DtoMapper.toResponse(client, activeSubs);
    }
}