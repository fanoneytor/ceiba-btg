package com.btg.fondos.application.service;

import com.btg.fondos.domain.model.Client;
import com.btg.fondos.infrastructure.repository.ClientRepository;
import com.btg.fondos.presentation.dto.ClientCreateRequest;
import com.btg.fondos.presentation.mapper.DtoMapper; // Import DtoMapper
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public Optional<Client> getById(String id) {
        return clientRepository.findById(id);
    }

    public Client save(Client client) {
        return clientRepository.save(client);
    }

    public List<Client> getAll() {
        return clientRepository.findAll();
    }

    public Client createClient(ClientCreateRequest req) {
        Client c = DtoMapper.toClient(req); // Use the mapper
        return clientRepository.save(c);
    }
}
