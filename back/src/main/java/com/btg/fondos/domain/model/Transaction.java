package com.btg.fondos.domain.model;

import com.btg.fondos.domain.enums.TransactionStatus;
import com.btg.fondos.domain.enums.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Document(collection = "transactions")
@CompoundIndexes({
        @CompoundIndex(name = "by_cliente_fecha_desc", def = "{'idCliente': 1, 'fecha': -1}")
})
public class Transaction {

    @Id
    private String id;

    @Indexed(unique = true)
    private String transactionId = UUID.randomUUID().toString();

    @NotBlank
    private String clientId;

    @NotBlank
    private String fundId;

    private TransactionType type;
    @Positive
    private BigDecimal amount;

    @CreatedDate
    private Instant date;

    private TransactionStatus status;
    private String message;

}
