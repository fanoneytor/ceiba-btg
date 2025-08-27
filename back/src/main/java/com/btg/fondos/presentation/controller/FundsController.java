package com.btg.fondos.presentation.controller;

import com.btg.fondos.presentation.mapper.DtoMapper;
import com.btg.fondos.presentation.dto.FundResponse;
import com.btg.fondos.application.service.FundService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/funds")
@CrossOrigin
public class FundsController {

    private final FundService fundService;

    public FundsController(FundService fundService) {
        this.fundService = fundService;
    }

    @GetMapping
    public ResponseEntity<List<FundResponse>> list() {
        var funds = fundService.getAll().stream()
                .map(DtoMapper::toResponse)
                .toList();
        return ResponseEntity.ok(funds);
    }
}
