package com.btg.fondos.config;

import com.btg.fondos.domain.enums.FundCategory;
import com.btg.fondos.domain.enums.NotificationChannel;
import com.btg.fondos.domain.model.Fund;
import com.btg.fondos.domain.model.Client;
import com.btg.fondos.infrastructure.repository.FundRepository;
import com.btg.fondos.infrastructure.repository.ClientRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner loadInitialFunds(FundRepository fundRepository) {
        return args -> {
            if (fundRepository.count() == 0) {
                List<Fund> funds = List.of(
                        new Fund(null, "FPV_BTG_PACTUAL_RECAUDADORA", new BigDecimal("75000"), FundCategory.FPV, null, null),
                        new Fund(null, "FPV_BTG_PACTUAL_ECOPETROL", new BigDecimal("125000"), FundCategory.FPV, null, null),
                        new Fund(null, "DEUDAPRIVADA", new BigDecimal("50000"), FundCategory.FIC, null, null),
                        new Fund(null, "FDO-ACCIONES", new BigDecimal("250000"), FundCategory.FIC, null, null),
                        new Fund(null, "FPV_BTG_PACTUAL_DINAMICA", new BigDecimal("100000"), FundCategory.FPV, null, null)
                );
                fundRepository.saveAll(funds);
                System.out.println("✅ Fondos iniciales cargados en MongoDB.");
            }
        };
    }

    @Bean
    CommandLineRunner loadInitialClient(ClientRepository clientRepository) {
        return args -> {
            if (clientRepository.count() == 0) {
                Client initialClient = Client.builder()
                        .name("Stefano Arias")
                        .email("stefano.arias@example.com")
                        .phone("3232230537")
                        .availableBalance(new BigDecimal("500000"))
                        .preferredNotification(NotificationChannel.EMAIL)
                        .build();
                clientRepository.save(initialClient);
                System.out.println("✅ Cliente inicial cargado en MongoDB.");
            }
        };
    }
}