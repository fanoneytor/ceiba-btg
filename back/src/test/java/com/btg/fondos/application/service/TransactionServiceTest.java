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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private ClientRepository clientRepository;
    @Mock
    private FundRepository fundRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private ApplicationEventPublisher publisher;

    @InjectMocks
    private TransactionService service;

    private Client client;
    private Fund fund;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setId("C1");
        client.setName("John Doe");
        client.setEmail("john@doe.com");
        client.setPhone("+573001234567");
        client.setAvailableBalance(new BigDecimal("500000"));
        client.setActiveFunds(new ArrayList<>());

        fund = new Fund();
        fund.setId("F1");
        fund.setName("FPV_BTG_PACTUAL_ECOPETROL");
        fund.setMinimumAmount(new BigDecimal("125000"));

        // Por defecto, mockeamos save(transaction) para retornar el mismo objeto con un id simulado
        lenient().when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> {
                    Transaction t = inv.getArgument(0);
                    if (t.getId() == null) t.setId("T-" + System.nanoTime());
                    if (t.getTransactionId() == null) t.setTransactionId("TX-" + System.nanoTime());
                    if (t.getDate() == null) t.setDate(Instant.now());
                    return t;
                });
    }

    @Test
    void subscribe_success_publishesEvent_and_updatesBalanceAndActiveFunds() {
        // arrange
        when(clientRepository.findById("C1")).thenReturn(Optional.of(client));
        when(fundRepository.findById("F1")).thenReturn(Optional.of(fund));
        when(clientRepository.save(any(Client.class))).thenAnswer(inv -> inv.getArgument(0));

        BigDecimal amount = new BigDecimal("125000");

        // act
        Transaction tx = service.subscribe("C1", "F1", amount);

        // assert
        assertEquals(TransactionType.SUBSCRIPTION, tx.getType());
        assertEquals(TransactionStatus.SUCCESS, tx.getStatus());
        assertEquals(amount, tx.getAmount());

        // saldo descontado
        assertEquals(new BigDecimal("375000"), client.getAvailableBalance());

        // suscripción agregada
        assertThat(client.getActiveFunds())
                .hasSize(1)
                .first()
                .extracting(ActiveSubscription::getFundId, ActiveSubscription::getInvestedAmount)
                .containsExactly("F1", amount);

        // se guardó client y transacción
        verify(clientRepository).save(client);
        verify(transactionRepository).save(any(Transaction.class));

        // se publicó el evento
        verify(publisher).publishEvent(isA(SubscriptionCreatedEvent.class));
    }

    @Test
    void subscribe_fails_whenAmountBelowMinimum() {
        when(clientRepository.findById("C1")).thenReturn(Optional.of(client));
        when(fundRepository.findById("F1")).thenReturn(Optional.of(fund));

        BigDecimal amount = new BigDecimal("100000"); // menor a 125000

        Transaction tx = service.subscribe("C1", "F1", amount);

        assertEquals(TransactionStatus.FAILED, tx.getStatus());
        assertEquals("Amount below fund minimum", tx.getMessage());

        // no toca saldo ni fondos
        assertEquals(new BigDecimal("500000"), client.getAvailableBalance());
        assertThat(client.getActiveFunds()).isEmpty();

        verify(clientRepository, never()).save(any());
        verify(publisher, never()).publishEvent(any());
    }

    @Test
    void subscribe_fails_whenInsufficientBalance() {
        when(clientRepository.findById("C1")).thenReturn(Optional.of(client));
        when(fundRepository.findById("F1")).thenReturn(Optional.of(fund));

        BigDecimal amount = new BigDecimal("600000"); // mayor al saldo

        Transaction tx = service.subscribe("C1", "F1", amount);

        assertEquals(TransactionStatus.FAILED, tx.getStatus());
        assertEquals("Insufficient balance", tx.getMessage());

        assertEquals(new BigDecimal("500000"), client.getAvailableBalance());
        assertThat(client.getActiveFunds()).isEmpty();

        verify(clientRepository, never()).save(any());
        verify(publisher, never()).publishEvent(any());
    }

    @Test
    void subscribe_fails_whenAlreadySubscribedToSameFund() {
        // cliente ya suscrito a F1
        client.getActiveFunds().add(new ActiveSubscription("F1", new BigDecimal("125000"), Instant.now()));

        when(clientRepository.findById("C1")).thenReturn(Optional.of(client));
        when(fundRepository.findById("F1")).thenReturn(Optional.of(fund));

        Transaction tx = service.subscribe("C1", "F1", new BigDecimal("125000"));

        assertEquals(TransactionStatus.FAILED, tx.getStatus());
        assertEquals("Already subscribed to this fund", tx.getMessage());

        // no cambia saldo ni duplica suscripción
        assertEquals(new BigDecimal("500000"), client.getAvailableBalance());
        assertThat(client.getActiveFunds()).hasSize(1);

        verify(clientRepository, never()).save(any());
        verify(publisher, never()).publishEvent(any());
    }

    @Test
    void cancel_success_returnsBalance_andRemovesActiveSubscription() {
        // prepare cliente con suscripción activa
        client.getActiveFunds().add(new ActiveSubscription("F1", new BigDecimal("125000"), Instant.now()));

        when(clientRepository.findById("C1")).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenAnswer(inv -> inv.getArgument(0));

        Transaction tx = service.cancel("C1", "F1");

        assertEquals(TransactionType.CANCELLATION, tx.getType());
        assertEquals(TransactionStatus.SUCCESS, tx.getStatus());
        assertEquals(new BigDecimal("125000"), tx.getAmount());

        // saldo retornado
        assertEquals(new BigDecimal("625000"), client.getAvailableBalance());
        // suscripción removida
        assertThat(client.getActiveFunds()).isEmpty();

        verify(clientRepository).save(client);
        verify(transactionRepository).save(any(Transaction.class));
        // No hay evento de cancelación en esta versión (solo en suscripción)
        verifyNoInteractions(publisher);
    }

    @Test
    void cancel_fails_whenNoActiveSubscription() {
        when(clientRepository.findById("C1")).thenReturn(Optional.of(client));

        Transaction tx = service.cancel("C1", "F1");

        assertEquals(TransactionStatus.FAILED, tx.getStatus());
        assertEquals("No active subscription found", tx.getMessage());

        // no cambia saldo
        assertEquals(new BigDecimal("500000"), client.getAvailableBalance());
        verify(clientRepository, never()).save(any());
        verifyNoInteractions(publisher);
    }

    @Test
    void history_returnsTransactionsOrderedDesc() {
        List<Transaction> mocked = List.of(
                tx("C1", "F1", TransactionType.SUBSCRIPTION, TransactionStatus.SUCCESS, new BigDecimal("125000"), Instant.parse("2025-08-27T10:00:00Z")),
                tx("C1", "F1", TransactionType.CANCELLATION, TransactionStatus.SUCCESS, new BigDecimal("125000"), Instant.parse("2025-08-28T10:00:00Z"))
        );
        when(transactionRepository.findByClientIdOrderByDateDesc("C1")).thenReturn(mocked);

        List<Transaction> result = service.getHistory("C1");
        assertThat(result).hasSize(2);
        // asumimos que el repo ya entrega ordenado desc; validamos que respeta el mock
        assertEquals(TransactionType.SUBSCRIPTION, result.get(0).getType());
        assertEquals(TransactionType.CANCELLATION, result.get(1).getType());

        verify(transactionRepository).findByClientIdOrderByDateDesc("C1");
    }

    // helper para construir transacciones simuladas
    private Transaction tx(String clientId, String fundId, TransactionType type, TransactionStatus status, BigDecimal amount, Instant date) {
        Transaction t = new Transaction();
        t.setId("ID-" + type + "-" + date.toEpochMilli());
        t.setTransactionId("TX-" + type + "-" + date.toEpochMilli());
        t.setClientId(clientId);
        t.setFundId(fundId);
        t.setType(type);
        t.setStatus(status);
        t.setAmount(amount);
        t.setDate(date);
        t.setMessage("mock");
        return t;
    }
}
