package com.btg.fondos.service;

import com.btg.fondos.domain.enums.TransactionStatus;
import com.btg.fondos.domain.enums.TransactionType;
import com.btg.fondos.domain.model.ActiveSubscription;
import com.btg.fondos.domain.model.Client;
import com.btg.fondos.domain.model.Fund;
import com.btg.fondos.domain.model.Transaction;
import com.btg.fondos.repository.ClientRepository;
import com.btg.fondos.repository.FundRepository;
import com.btg.fondos.repository.TransactionRepository;
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

    public TransactionService(ClientRepository clientRepository,
                              FundRepository fundRepository,
                              TransactionRepository transactionRepository) {
        this.clientRepository = clientRepository;
        this.fundRepository = fundRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Transaction subscribe(String clientId, String fundId, BigDecimal amount) {
        Optional<Client> clientOpt = clientRepository.findById(clientId);
        Optional<Fund> fundOpt = fundRepository.findById(fundId);

        if (clientOpt.isEmpty() || fundOpt.isEmpty()) {
            return saveTransaction(clientId, fundId, amount, TransactionType.SUBSCRIPTION,
                    TransactionStatus.FAILED, "Client or Fund not found");
        }

        Client client = clientOpt.get();
        Fund fund = fundOpt.get();

        if (amount.compareTo(fund.getMinimumAmount()) < 0) {
            return saveTransaction(clientId, fundId, amount, TransactionType.SUBSCRIPTION,
                    TransactionStatus.FAILED, "Amount below fund minimum");
        }

        if (client.getAvailableBalance().compareTo(amount) < 0) {
            return saveTransaction(clientId, fundId, amount, TransactionType.SUBSCRIPTION,
                    TransactionStatus.FAILED, "Insufficient balance");
        }

        client.setAvailableBalance(client.getAvailableBalance().subtract(amount));
        client.getActiveFunds().add(new ActiveSubscription(fundId, amount, Instant.now()));
        clientRepository.save(client);

        return saveTransaction(clientId, fundId, amount, TransactionType.SUBSCRIPTION,
                TransactionStatus.SUCCESS, "Subscription successful");
    }

    @Transactional
    public Transaction cancel(String clientId, String fundId) {
        Optional<Client> clientOpt = clientRepository.findById(clientId);

        if (clientOpt.isEmpty()) {
            return saveTransaction(clientId, fundId, BigDecimal.ZERO, TransactionType.CANCELLATION,
                    TransactionStatus.FAILED, "Client not found");
        }

        Client client = clientOpt.get();

        ActiveSubscription subscription = client.getActiveFunds().stream()
                .filter(s -> s.getFundId().equals(fundId))
                .findFirst()
                .orElse(null);

        if (subscription == null) {
            return saveTransaction(clientId, fundId, BigDecimal.ZERO, TransactionType.CANCELLATION,
                    TransactionStatus.FAILED, "No active subscription found");
        }

        client.setAvailableBalance(client.getAvailableBalance().add(subscription.getInvestedAmount()));
        client.getActiveFunds().remove(subscription);
        clientRepository.save(client);

        return saveTransaction(clientId, fundId, subscription.getInvestedAmount(),
                TransactionType.CANCELLATION, TransactionStatus.SUCCESS, "Cancellation successful");
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
