package com.btg.fondos.domain.model;

import com.btg.fondos.domain.enums.NotificationChannel;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "clients")
public class Client {

    @Id
    private String id;

    @NotBlank
    private String name;

    @Email
    @Indexed(unique = true, sparse = true)
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Teléfono inválido")
    @Indexed(unique = true, sparse = true)
    private String phone;

    @PositiveOrZero
    private BigDecimal availableBalance;

    private NotificationChannel preferredNotification = NotificationChannel.EMAIL;

    private List<ActiveSubscription> activeFunds = new ArrayList<>();

    @Version
    private Long version;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

}
