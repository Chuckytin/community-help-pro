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

        log.debug("===[skill-sync] Skills verification ===");
        log.debug("Total skills in enum: {}", allSkills.size());

        List<String> existingSkills = jdbcTemplate.queryForList(
                "SELECT DISTINCT skills FROM volunteer_skills", String.class
        );

        log.debug("Total skills currently in use by volunteers: {}", existingSkills.size());

        Set<String> unusedSkills = allSkills.stream()
                .map(Enum::name)
                .filter(skill -> !existingSkills.contains(skill))
                .collect(Collectors.toSet());

        if (!unusedSkills.isEmpty()) {
            log.debug("New skills available (no volunteer has them yet): {}",
                    unusedSkills.stream().sorted().collect(Collectors.joining(", ")));
        } else {
            log.debug("All enum skills are being used by at least one volunteer");
        }

        log.debug("=== [skill-sync] End of verification ===");
    }
}