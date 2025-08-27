package com.btg.fondos.repository;

import com.btg.fondos.domain.model.Client;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ClientRepository extends MongoRepository<Client, String> {
}
