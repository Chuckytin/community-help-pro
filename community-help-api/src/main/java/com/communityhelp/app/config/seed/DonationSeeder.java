package com.communityhelp.app.config.seed;

import com.communityhelp.app.donation.event.DonationCreatedEvent;
import com.communityhelp.app.donation.model.Donation;
import com.communityhelp.app.donation.model.DonationStatus;
import com.communityhelp.app.donation.model.DonationType;
import com.communityhelp.app.donation.model.FoodType;
import com.communityhelp.app.donation.repository.DonationRepository;
import com.communityhelp.app.user.model.User;
import com.communityhelp.app.user.repository.UserRepository;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * Crea donaciones de prueba asociadas a los usuarios sembrados por UserSeeder,
 * publicando DonationCreatedEvent para disparar la generación automática de
 * proposals, igual que en el flujo real de la API.
 */
@Component
@Profile({"dev", "local"})
@Order(2)
@Slf4j
public class DonationSeeder implements CommandLineRunner {

    private final DonationRepository donationRepository;
    private final UserRepository userRepository;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final TransactionTemplate transactionTemplate;

    public DonationSeeder(
            DonationRepository donationRepository,
            UserRepository userRepository,
            ResourceLoader resourceLoader,
            ObjectMapper objectMapper,
            ApplicationEventPublisher eventPublisher,
            PlatformTransactionManager transactionManager
    ) {
        this.donationRepository = donationRepository;
        this.userRepository = userRepository;
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    static class DonationSeedDto {
        private String donorEmail;
        private String title;
        private String description;
        private String donationType;
        private String foodType;
        private Integer quantity;
        private String unit;
        private Double latitude;
        private Double longitude;
    }

    @Override
    public void run(String @NonNull ... args) throws Exception {

        if (donationRepository.count() > 0) {
            log.info("Seeder omitido: ya existen donaciones en la base de datos.");
            return;
        }

        Resource resource = resourceLoader.getResource("classpath:seed/donations.json");

        List<DonationSeedDto> seedDtos = objectMapper.readValue(
                resource.getInputStream(),
                new TypeReference<>() {
                }
        );

        int created = 0;
        for (DonationSeedDto dto : seedDtos) {
            if (seedOne(dto)) {
                created++;
            }
        }

        log.info("Seeder completado: {} donaciones de prueba insertadas.", created);
    }

    /**
     * Guarda una donación y publica su evento de creación dentro de la misma
     * transacción, para que el TransactionalEventListener (AFTER_COMMIT)
     * del motor de matching se dispare igual que en el flujo real de la API.
     */
    private boolean seedOne(DonationSeedDto dto) {
        return Boolean.TRUE.equals(transactionTemplate.execute(status -> {

            User donor = userRepository.findByEmailIncludeInactive(dto.getDonorEmail())
                    .orElse(null);

            if (donor == null) {
                log.warn("Seeder de donaciones: donante no encontrado, email={}", dto.getDonorEmail());
                return false;
            }

            Donation donation = Donation.builder()
                    .donor(donor)
                    .title(dto.getTitle())
                    .description(dto.getDescription())
                    .donationType(DonationType.valueOf(dto.getDonationType()))
                    .foodType(dto.getFoodType() != null ? FoodType.valueOf(dto.getFoodType()) : null)
                    .quantity(dto.getQuantity())
                    .unit(dto.getUnit())
                    .status(DonationStatus.AVAILABLE)
                    .active(true)
                    .build();

            if (dto.getLatitude() != null && dto.getLongitude() != null) {
                donation.setLocation(dto.getLatitude(), dto.getLongitude());
            }

            Donation saved = donationRepository.save(donation);

            eventPublisher.publishEvent(new DonationCreatedEvent(saved.getId()));

            return true;
        }));
    }
}