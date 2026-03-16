package com.communityhelp.app.proposal.util;

import com.communityhelp.app.donation.model.DonationType;
import com.communityhelp.app.helprequest.model.HelpRequestType;
import com.communityhelp.app.volunteer.model.VolunteerSkill;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * Clase que mapea tipos de HelpRequest y Donation a las skills de voluntarios necesarias.
 * Se usa en ProposalGeneratorService para calcular el score inicial de una propuesta.
 */
public class SkillMatcher {

    /**
     * Skills relevantes por tipo de HelpRequest.
     */
    public static final Map<HelpRequestType, Set<VolunteerSkill>> HELP_REQUEST_SKILLS = new EnumMap<>(HelpRequestType.class);

    /**
     * Skills relevantes por tipo de Donation.
     */
    public static final Map<DonationType, Set<VolunteerSkill>> DONATION_SKILLS = new EnumMap<>(DonationType.class);

    static {
        // HELP_REQUEST
        HELP_REQUEST_SKILLS.put(HelpRequestType.FOOD, Set.of(
                VolunteerSkill.FOOD_HANDLING,
                VolunteerSkill.LOGISTICS,
                VolunteerSkill.COOKING,
                VolunteerSkill.CATERING,
                VolunteerSkill.SHOPPING
        ));

        HELP_REQUEST_SKILLS.put(HelpRequestType.TRANSPORT, Set.of(
                VolunteerSkill.TRANSPORT,
                VolunteerSkill.DRIVING,
                VolunteerSkill.LOGISTICS
        ));

        HELP_REQUEST_SKILLS.put(HelpRequestType.COMPANIONSHIP, Set.of(
                VolunteerSkill.COMMUNICATION,
                VolunteerSkill.PATIENT,
                VolunteerSkill.ELDERLY_CARE,
                VolunteerSkill.BABY_CARE,
                VolunteerSkill.ELDERLY_SUPPORT,
                VolunteerSkill.DISABILITY_SUPPORT,
                VolunteerSkill.PSYCHOLOGICAL_SUPPORT,
                VolunteerSkill.SHOPPING,
                VolunteerSkill.TRANSLATION
        ));

        HELP_REQUEST_SKILLS.put(HelpRequestType.MEDICAL, Set.of(
                VolunteerSkill.MEDICAL_ASSISTANCE,
                VolunteerSkill.PATIENT,
                VolunteerSkill.LOGISTICS,
                VolunteerSkill.FIRST_AID,
                VolunteerSkill.CRISIS_MANAGEMENT,
                VolunteerSkill.EMERGENCY_RESPONSE,
                VolunteerSkill.TRANSLATION
        ));

        HELP_REQUEST_SKILLS.put(HelpRequestType.EDUCATION, Set.of(
                VolunteerSkill.COMMUNICATION,
                VolunteerSkill.LANGUAGE,
                VolunteerSkill.PATIENT,
                VolunteerSkill.TEACHING,
                VolunteerSkill.WRITING
        ));

        HELP_REQUEST_SKILLS.put(HelpRequestType.PET_CARE, Set.of(
                VolunteerSkill.PET_CARE,
                VolunteerSkill.PATIENT,
                VolunteerSkill.LOGISTICS,
                VolunteerSkill.SHOPPING
        ));

        HELP_REQUEST_SKILLS.put(HelpRequestType.BABY_CARE, Set.of(
                VolunteerSkill.BABY_CARE,
                VolunteerSkill.PATIENT,
                VolunteerSkill.COMMUNICATION,
                VolunteerSkill.CHILD_CARE,
                VolunteerSkill.SHOPPING
        ));

        HELP_REQUEST_SKILLS.put(HelpRequestType.COMMUNITY_EVENTS, Set.of(
                VolunteerSkill.EVENT_PLANNING,
                VolunteerSkill.COMMUNICATION,
                VolunteerSkill.LOGISTICS,
                VolunteerSkill.SOCIAL_MEDIA,
                VolunteerSkill.PHOTOGRAPHY,
                VolunteerSkill.WRITING,
                VolunteerSkill.TRANSLATION
        ));

        HELP_REQUEST_SKILLS.put(HelpRequestType.EMERGENCY, Set.of(
                VolunteerSkill.MEDICAL_ASSISTANCE,
                VolunteerSkill.DRIVING,
                VolunteerSkill.PHYSICAL_LABOR,
                VolunteerSkill.LOGISTICS,
                VolunteerSkill.FIRST_AID,
                VolunteerSkill.CRISIS_MANAGEMENT,
                VolunteerSkill.EMERGENCY_RESPONSE,
                VolunteerSkill.TRANSLATION
        ));

        HELP_REQUEST_SKILLS.put(HelpRequestType.OTHER, Set.of(
                VolunteerSkill.GARDENING,
                VolunteerSkill.LANGUAGE,
                VolunteerSkill.EVENT_PLANNING,
                VolunteerSkill.PHYSICAL_LABOR,
                VolunteerSkill.HEAVY_LIFTING,
                VolunteerSkill.ADMINISTRATIVE,
                VolunteerSkill.CARPENTRY,
                VolunteerSkill.ELECTRICIAN,
                VolunteerSkill.PLUMBING,
                VolunteerSkill.PAINTING,
                VolunteerSkill.CLEANING,
                VolunteerSkill.SHOPPING,
                VolunteerSkill.TRANSLATION
        ));

        // DONATION
        DONATION_SKILLS.put(DonationType.FOOD, Set.of(
                VolunteerSkill.FOOD_HANDLING,
                VolunteerSkill.LOGISTICS,
                VolunteerSkill.COOKING,
                VolunteerSkill.CATERING,
                VolunteerSkill.SHOPPING
        ));

        DONATION_SKILLS.put(DonationType.CLOTHING, Set.of(
                VolunteerSkill.LOGISTICS,
                VolunteerSkill.SHOPPING
        ));

        DONATION_SKILLS.put(DonationType.HYGIENE, Set.of(
                VolunteerSkill.LOGISTICS,
                VolunteerSkill.SHOPPING
        ));

        DONATION_SKILLS.put(DonationType.TOYS, Set.of(
                VolunteerSkill.LOGISTICS,
                VolunteerSkill.BABY_CARE,
                VolunteerSkill.CHILD_CARE,
                VolunteerSkill.SHOPPING
        ));

        DONATION_SKILLS.put(DonationType.FURNITURE, Set.of(
                VolunteerSkill.LOGISTICS,
                VolunteerSkill.HEAVY_LIFTING,
                VolunteerSkill.PHYSICAL_LABOR,
                VolunteerSkill.CARPENTRY
        ));

        DONATION_SKILLS.put(DonationType.ELECTRONICS, Set.of(
                VolunteerSkill.LOGISTICS,
                VolunteerSkill.TECH,
                VolunteerSkill.ELECTRICIAN,
                VolunteerSkill.SHOPPING
        ));

        DONATION_SKILLS.put(DonationType.MEDICAL_SUPPLIES, Set.of(
                VolunteerSkill.MEDICAL_ASSISTANCE,
                VolunteerSkill.LOGISTICS,
                VolunteerSkill.FIRST_AID
        ));

        DONATION_SKILLS.put(DonationType.BOOKS, Set.of(
                VolunteerSkill.COMMUNICATION,
                VolunteerSkill.LANGUAGE,
                VolunteerSkill.WRITING,
                VolunteerSkill.TEACHING,
                VolunteerSkill.TRANSLATION
        ));

        DONATION_SKILLS.put(DonationType.STATIONERY, Set.of(
                VolunteerSkill.LOGISTICS,
                VolunteerSkill.COMMUNICATION,
                VolunteerSkill.WRITING
        ));

        DONATION_SKILLS.put(DonationType.VEHICLES, Set.of(
                VolunteerSkill.DRIVING,
                VolunteerSkill.HEAVY_LIFTING,
                VolunteerSkill.LOGISTICS
        ));

        DONATION_SKILLS.put(DonationType.COMMUNITY_EQUIPMENT, Set.of(
                VolunteerSkill.LOGISTICS,
                VolunteerSkill.PHYSICAL_LABOR,
                VolunteerSkill.EVENT_PLANNING
        ));

        DONATION_SKILLS.put(DonationType.OTHER, Set.of(
                VolunteerSkill.EVENT_PLANNING,
                VolunteerSkill.LANGUAGE,
                VolunteerSkill.GARDENING,
                VolunteerSkill.PHYSICAL_LABOR,
                VolunteerSkill.HEAVY_LIFTING,
                VolunteerSkill.COMMUNICATION,
                VolunteerSkill.ADMINISTRATIVE,
                VolunteerSkill.LEGAL_ADVICE,
                VolunteerSkill.FINANCIAL_ADVICE,
                VolunteerSkill.SHOPPING,
                VolunteerSkill.TRANSLATION
        ));
    }
}