package com.btg.fondos.application.service;

import com.btg.fondos.domain.enums.TransactionStatus;
import com.btg.fondos.domain.enums.TransactionType;
import com.btg.fondos.domain.events.SubscriptionCreatedEvent;
import com.btg.fondos.domain.model.ActiveSubscription;
import com.btg.fondos.domain.model.Client;
import com.btg.fondos.domain.model.Fund;
import com.btg.fondos.domain.model.Transaction;
import com.btg.fondos.infrastructure.repository.ClientRepository;
import com.btg.fondos.infrastructure.repository.FundRepository;
import com.btg.fondos.infrastructure.repository.TransactionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    private final ClientRepository clientRepository;
    private final FundRepository fundRepository;
    private final TransactionRepository transactionRepository;
    private final ApplicationEventPublisher publisher;

    public TransactionService(ClientRepository clientRepository,
                              FundRepository fundRepository,
                              TransactionRepository transactionRepository,
                              ApplicationEventPublisher publisher) {
        this.clientRepository = clientRepository;
        this.fundRepository = fundRepository;
        this.transactionRepository = transactionRepository;
        this.publisher = publisher;
    }

    @Transactional
    public Transaction subscribe(String clientId, String fundId, BigDecimal amount) {
        Optional<Client> clientOpt = clientRepository.findById(clientId);
        Optional<Fund> fundOpt = fundRepository.findById(fundId);

        if (clientOpt.isEmpty() || fundOpt.isEmpty()) {
            return saveTransaction(clientId, fundId, amount, TransactionType.SUBSCRIPTION,
                    TransactionStatus.FAILED, "Cliente o Fondo no encontrado");
        }

        Client client = clientOpt.get();
        Fund fund = fundOpt.get();

        boolean alreadySubscribed = client.getActiveFunds().stream()
                .anyMatch(s -> s.getFundId().equals(fundId));
        if (alreadySubscribed) {
            return saveTransaction(clientId, fundId, amount, TransactionType.SUBSCRIPTION,
                    TransactionStatus.FAILED, "Ya estas suscrito a este fondo");
        }

        if (amount.compareTo(fund.getMinimumAmount()) < 0) {
            return saveTransaction(clientId, fundId, amount, TransactionType.SUBSCRIPTION,
                    TransactionStatus.FAILED, "Monto por debajo del minimo");
        }

        if (client.getAvailableBalance().compareTo(amount) < 0) {
            return saveTransaction(clientId, fundId, amount, TransactionType.SUBSCRIPTION,
                    TransactionStatus.FAILED, "No tiene saldo disponible para vincularse al fondo " + fund.getName());
        }

        client.setAvailableBalance(client.getAvailableBalance().subtract(amount));
        client.getActiveFunds().add(new ActiveSubscription(fundId, amount, Instant.now()));
        clientRepository.save(client);

        Transaction tx = saveTransaction(clientId, fundId, amount, TransactionType.SUBSCRIPTION,
                TransactionStatus.SUCCESS, "Suscripción exitosa");

        publisher.publishEvent(new SubscriptionCreatedEvent(this, clientId, fundId, tx.getTransactionId())); // <-- nuevo
        return tx;
    }

    @Transactional
    public Transaction cancel(String clientId, String fundId) {
        Optional<Client> clientOpt = clientRepository.findById(clientId);

        if (clientOpt.isEmpty()) {
            return saveTransaction(clientId, fundId, BigDecimal.ZERO, TransactionType.CANCELLATION,
                    TransactionStatus.FAILED, "Cliente no encontrado");
        }

        Client client = clientOpt.get();

        ActiveSubscription subscription = client.getActiveFunds().stream()
                .filter(s -> s.getFundId().equals(fundId))
                .findFirst()
                .orElse(null);

        if (subscription == null) {
            return saveTransaction(clientId, fundId, BigDecimal.ZERO, TransactionType.CANCELLATION,
                    TransactionStatus.FAILED, "No se encontró ninguna suscripción activa");
        }

        client.setAvailableBalance(client.getAvailableBalance().add(subscription.getInvestedAmount()));
        client.getActiveFunds().remove(subscription);
        clientRepository.save(client);

        return saveTransaction(clientId, fundId, subscription.getInvestedAmount(),
                TransactionType.CANCELLATION, TransactionStatus.SUCCESS, "Cancelación exitosa");
    }

    public List<Transaction> getHistory(String clientId) {
        return transactionRepository.findByClientIdOrderByDateDesc(clientId);
    }

    private Transaction saveTransaction(String clientId, String fundId, BigDecimal amount,
                                        TransactionType type, TransactionStatus status, String message) {
        Transaction t = new Transaction();
        t.setClientId(clientId);
        t.setFundId(fundId);
        t.setType(type);
        t.setAmount(amount);
        t.setStatus(status);
        t.setMessage(message);
        t.setDate(Instant.now());
        return transactionRepository.save(t);
    }
}
