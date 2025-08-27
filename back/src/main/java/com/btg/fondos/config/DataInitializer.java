package com.btg.fondos.config;

import com.btg.fondos.domain.enums.FundCategory;
import com.btg.fondos.domain.model.Fund;
import com.btg.fondos.infrastructure.repository.FundRepository;
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
                System.out.println("✅ Initial funds loaded into MongoDB.");
            }
        };
    }
}
