package com.btg.fondos.application.service;

import com.btg.fondos.domain.model.Fund;
import com.btg.fondos.infrastructure.repository.FundRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FundService {

    private final FundRepository fundRepository;

    public FundService(FundRepository fundRepository) {
        this.fundRepository = fundRepository;
    }

    public List<Fund> getAll() {
        return fundRepository.findAll();
    }

    public Optional<Fund> getById(String id) {
        return fundRepository.findById(id);
    }
}
