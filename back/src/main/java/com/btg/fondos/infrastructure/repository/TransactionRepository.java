package com.btg.fondos.infrastructure.repository;

import com.btg.fondos.domain.model.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TransactionRepository extends MongoRepository<Transaction, String> {
    List<Transaction> findByClientIdOrderByDateDesc(String clientId);
}
