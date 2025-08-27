package com.btg.fondos.presentation.controller;

import com.btg.fondos.presentation.dto.CancellationRequest;
import com.btg.fondos.presentation.mapper.DtoMapper;
import com.btg.fondos.presentation.dto.SubscriptionRequest;
import com.btg.fondos.presentation.dto.TransactionResponse;
import com.btg.fondos.application.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin
public class TransactionsController {

    private final TransactionService transactionService;

    public TransactionsController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/subscribe")
    public ResponseEntity<TransactionResponse> subscribe(@Valid @RequestBody SubscriptionRequest req) {
        var tx = transactionService.subscribe(req.clientId(), req.fundId(), req.amount());
        return ResponseEntity.ok(DtoMapper.toResponse(tx));
    }

    @PostMapping("/cancel")
    public ResponseEntity<TransactionResponse> cancel(@Valid @RequestBody CancellationRequest req) {
        var tx = transactionService.cancel(req.clientId(), req.fundId());
        return ResponseEntity.ok(DtoMapper.toResponse(tx));
    }

    @GetMapping("/history/{clientId}")
    public ResponseEntity<java.util.List<TransactionResponse>> history(@PathVariable String clientId) {
        var list = transactionService.getHistory(clientId).stream()
                .map(DtoMapper::toResponse)
                .toList();
        return ResponseEntity.ok(list);
    }
}
