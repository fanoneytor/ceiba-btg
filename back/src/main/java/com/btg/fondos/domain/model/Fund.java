package com.btg.fondos.domain.model;

import com.btg.fondos.domain.enums.FundCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Document(collection = "funds")
public class Fund {

    @Id
    private String id;

    @NotBlank
    @Indexed(unique = true)
    private String name;

    @Positive
    private BigDecimal minimumAmount;

    private FundCategory category;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

}
