package com.communityhelp.app.volunteer.service;

import com.communityhelp.app.volunteer.model.VolunteerSkill;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class VolunteerSkillSynchronizer {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Verifica que skills existen en la Base de datos.
     * Verifica que skills están en el enum VolunteerSkill y que aún no los usa ningún voluntario.
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void verifySkillsInDatabase() {
        Set<VolunteerSkill> allSkills = EnumSet.allOf(VolunteerSkill.class);

        log.debug("===[skill-sync] Verificación de Skills ===");
        log.debug("Total de skills en el enum: {}", allSkills.size());

        List<String> existingSkills = jdbcTemplate.queryForList(
                "SELECT DISTINCT skills FROM volunteer_skills", String.class
        );

        log.debug("Total de skills actualmente en uso por voluntarios: {}", existingSkills.size());

        Set<String> unusedSkills = allSkills.stream()
                .map(Enum::name)
                .filter(skill -> !existingSkills.contains(skill))
                .collect(Collectors.toSet());

        if (!unusedSkills.isEmpty()) {
            log.debug("Nuevos skills disponibles (ningún voluntario los tiene aún): {}",
                    unusedSkills.stream().sorted().collect(Collectors.joining(", ")));
        } else {
            log.debug("Todos los skills del enum están siendo usados por al menos un voluntario");
        }

        log.debug("=== [skill-sync] Fin de verificación ===");
    }
}