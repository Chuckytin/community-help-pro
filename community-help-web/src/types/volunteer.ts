export type TransportMode = "FOOT_WALKING" | "CYCLING_REGULAR" | "DRIVING_CAR";

export type VolunteerSkill =
    | "TRANSPORT" | "FOOD_HANDLING" | "MEDICAL_ASSISTANCE" | "ELDERLY_CARE"
    | "SHOPPING" | "COMMUNICATION" | "COMPANIONSHIP" | "PET_CARE" | "BABY_CARE"
    | "TECH" | "HEAVY_LIFTING" | "PATIENT" | "LANGUAGE" | "EVENT_PLANNING"
    | "GARDENING" | "PHYSICAL_LABOR" | "LOGISTICS" | "DRIVING" | "FIRST_AID"
    | "CRISIS_MANAGEMENT" | "PSYCHOLOGICAL_SUPPORT" | "ADMINISTRATIVE" | "TEACHING"
    | "CHILD_CARE" | "ELDERLY_SUPPORT" | "DISABILITY_SUPPORT" | "EMERGENCY_RESPONSE"
    | "TRANSLATION" | "LEGAL_ADVICE" | "FINANCIAL_ADVICE" | "SOCIAL_MEDIA"
    | "PHOTOGRAPHY" | "WRITING" | "CARPENTRY" | "ELECTRICIAN" | "PLUMBING"
    | "PAINTING" | "CLEANING" | "COOKING" | "CATERING";

export interface VolunteerProfile {
    userId: string;
    name: string;
    email: string;
    available: boolean;
    radiusKm: number | null;
    skills: VolunteerSkill[];
    transportMode: TransportMode | null;
    emailNotificationsEnabled: boolean;
    latitude: number | null;
    longitude: number | null;
    rating: number | null;
}

/** Etiquetas legibles para las skills */
export const SKILL_LABELS: Record<VolunteerSkill, string> = {
    TRANSPORT: "Transporte", FOOD_HANDLING: "Manejo de alimentos",
    MEDICAL_ASSISTANCE: "Asistencia médica", ELDERLY_CARE: "Cuidado de mayores",
    SHOPPING: "Compras", COMMUNICATION: "Comunicación",
    COMPANIONSHIP: "Acompañamiento", PET_CARE: "Cuidado de mascotas",
    BABY_CARE: "Cuidado de bebés", TECH: "Tecnología",
    HEAVY_LIFTING: "Carga pesada", PATIENT: "Paciencia",
    LANGUAGE: "Idiomas", EVENT_PLANNING: "Organización de eventos",
    GARDENING: "Jardinería", PHYSICAL_LABOR: "Trabajo físico",
    LOGISTICS: "Logística", DRIVING: "Conducción",
    FIRST_AID: "Primeros auxilios", CRISIS_MANAGEMENT: "Gestión de crisis",
    PSYCHOLOGICAL_SUPPORT: "Apoyo psicológico", ADMINISTRATIVE: "Administrativo",
    TEACHING: "Enseñanza", CHILD_CARE: "Cuidado de niños",
    ELDERLY_SUPPORT: "Apoyo a mayores", DISABILITY_SUPPORT: "Apoyo a discapacitados",
    EMERGENCY_RESPONSE: "Respuesta a emergencias", TRANSLATION: "Traducción",
    LEGAL_ADVICE: "Asesoría legal", FINANCIAL_ADVICE: "Asesoría financiera",
    SOCIAL_MEDIA: "Redes sociales", PHOTOGRAPHY: "Fotografía",
    WRITING: "Escritura", CARPENTRY: "Carpintería",
    ELECTRICIAN: "Electricidad", PLUMBING: "Fontanería",
    PAINTING: "Pintura", CLEANING: "Limpieza",
    COOKING: "Cocina", CATERING: "Catering",
};

export const TRANSPORT_LABELS: Record<TransportMode, string> = {
    FOOT_WALKING: "🚶 A pie",
    CYCLING_REGULAR: "🚲 Bicicleta",
    DRIVING_CAR: "🚗 Coche",
};