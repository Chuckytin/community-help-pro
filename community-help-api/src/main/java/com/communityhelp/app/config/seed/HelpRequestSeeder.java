package com.communityhelp.app.config.seed;

import com.communityhelp.app.helprequest.event.HelpRequestCreatedEvent;
import com.communityhelp.app.helprequest.model.HelpRequest;
import com.communityhelp.app.helprequest.model.HelpRequestStatus;
import com.communityhelp.app.helprequest.model.HelpRequestType;
import com.communityhelp.app.helprequest.repository.HelpRequestRepository;
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

import java.time.LocalDateTime;
import java.util.List;

/**
 * Crea solicitudes de ayuda de prueba asociadas a los usuarios sembrados
 * por UserSeeder, publicando HelpRequestCreatedEvent para disparar la
 * generación automática de proposals.
 */
@Component
@Profile({"dev", "local"})
@Order(3)
@Slf4j
public class HelpRequestSeeder implements CommandLineRunner {

    private final HelpRequestRepository helpRequestRepository;
    private final UserRepository userRepository;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final TransactionTemplate transactionTemplate;

    public HelpRequestSeeder(
            HelpRequestRepository helpRequestRepository,
            UserRepository userRepository,
            ResourceLoader resourceLoader,
            ObjectMapper objectMapper,
            ApplicationEventPublisher eventPublisher,
            PlatformTransactionManager transactionManager
    ) {
        this.helpRequestRepository = helpRequestRepository;
        this.userRepository = userRepository;
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    static class HelpRequestSeedDto {
        private String requesterEmail;
        private String title;
        private String description;
        private String type;
        private Integer deadlineDaysFromNow;
        private Double latitude;
        private Double longitude;
    }

    @Override
    public void run(String @NonNull ... args) throws Exception {

        if (helpRequestRepository.count() > 0) {
            log.info("Seeder omitido: ya existen solicitudes de ayuda en la base de datos.");
            return;
        }

        Resource resource = resourceLoader.getResource("classpath:seed/helprequests.json");

        List<HelpRequestSeedDto> seedDtos = objectMapper.readValue(
                resource.getInputStream(),
                new TypeReference<>() {
                }
        );

        int created = 0;
        for (HelpRequestSeedDto dto : seedDtos) {
            if (seedOne(dto)) {
                created++;
            }
        }

        log.info("Seeder completado: {} solicitudes de ayuda de prueba insertadas.", created);
    }

    /**
     * Guarda una helpRequest y publica su evento de creación dentro de la misma
     * transacción, para que el TransactionalEventListener (AFTER_COMMIT)
     * del motor de matching se dispare igual que en el flujo real de la API.
     */
    private boolean seedOne(HelpRequestSeedDto dto) {
        return Boolean.TRUE.equals(transactionTemplate.execute(status -> {

            User requester = userRepository.findByEmailIncludeInactive(dto.getRequesterEmail())
                    .orElse(null);

            if (requester == null) {
                log.warn("Seeder de solicitudes: solicitante no encontrado, email={}", dto.getRequesterEmail());
                return false;
            }

            HelpRequest helpRequest = HelpRequest.builder()
                    .requester(requester)
                    .title(dto.getTitle())
                    .description(dto.getDescription())
                    .type(HelpRequestType.valueOf(dto.getType()))
                    .deadline(LocalDateTime.now().plusDays(dto.getDeadlineDaysFromNow()))
                    .status(HelpRequestStatus.OPEN)
                    .active(true)
                    .build();

            if (dto.getLatitude() != null && dto.getLongitude() != null) {
                helpRequest.setLocation(dto.getLatitude(), dto.getLongitude());
            }

            HelpRequest saved = helpRequestRepository.save(helpRequest);

            eventPublisher.publishEvent(new HelpRequestCreatedEvent(saved.getId()));

            return true;
        }));
    }
}