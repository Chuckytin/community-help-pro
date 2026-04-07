package com.communityhelp.app.config;

import com.communityhelp.app.donation.model.Donation;
import com.communityhelp.app.donation.model.DonationStatus;
import com.communityhelp.app.donation.repository.DonationRepository;
import com.communityhelp.app.helprequest.model.HelpRequest;
import com.communityhelp.app.helprequest.model.HelpRequestStatus;
import com.communityhelp.app.helprequest.repository.HelpRequestRepository;
import com.communityhelp.app.proposal.service.ProposalGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Componente que se ejecuta al iniciar la aplicación para generar proposals para HelpRequests y Donations existentes.
 * Útil para entornos de desarrollo o cuando se despliega una nueva versión con cambios en el motor de matching.
 * Se ejecuta en un hilo separado para no bloquear el arranque de la aplicación y solo si la propiedad "matching.run-on-startup" está habilitada.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MatchingRunner implements CommandLineRunner {

    @Value("${matching.run-on-startup:false}")
    private boolean runOnStartup;

    private final ProposalGeneratorService proposalGeneratorService;
    private final HelpRequestRepository helpRequestRepository;
    private final DonationRepository donationRepository;

    /**
     * Ejecuta un proceso al iniciar la aplicación para generar proposals para HelpRequests y Donations existentes.
     * Solo se ejecuta si la propiedad "matching.run-on-startup" está habilitada.
     */
    @Override
    public void run(String @NonNull ... args) {

        if (!runOnStartup) {
            log.info("MatchingRunner disabled (run-on-startup=false)");
            return;
        }

        new Thread(() -> {
            try {
                Thread.sleep(10000);
                log.info("Executing initial matching for existing data...");

                List<HelpRequest> helpRequests = helpRequestRepository.findAll();
                for (HelpRequest hr : helpRequests) {
                    if (hr.isActive() && hr.getStatus() == HelpRequestStatus.OPEN) {
                        proposalGeneratorService.generateForHelpRequest(hr);
                    }
                }

                List<Donation> donations = donationRepository.findAll();
                for (Donation d : donations) {
                    if (d.isActive() && d.getStatus() == DonationStatus.AVAILABLE) {
                        proposalGeneratorService.generateForDonation(d);
                    }
                }
            } catch (Exception e) {
                log.error("Error in initial matching", e);
            }
        }).start();
    }
}
