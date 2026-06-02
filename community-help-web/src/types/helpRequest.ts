export type HelpRequestStatus = "OPEN" | "ACCEPTED" | "COMPLETED" | "CANCELLED" | "EXPIRED";

export type HelpRequestType =
    | "FOOD" | "TRANSPORT" | "COMPANIONSHIP" | "MEDICAL" | "EDUCATION"
    | "PET_CARE" | "BABY_CARE" | "COMMUNITY_EVENTS" | "EMERGENCY" | "SHOPPING" | "OTHER";

export type TransportMode = "FOOT_WALKING" | "CYCLING_REGULAR" | "DRIVING_CAR";

export interface HelpRequest {
    id: string;
    requesterId: string;
    volunteerId: string | null;
    type: HelpRequestType;
    title: string;
    description: string;
    latitude: number | null;
    longitude: number | null;
    deadline: string | null;
    status: HelpRequestStatus;
    active: boolean;
    cancelReason: string | null;
    createdAt: string;
    updatedAt: string;
    acceptedAt: string | null;
    completedAt: string | null;
    estimatedTravelSeconds: number | null;
    estimatedDistanceMeters: number | null;
    usedTransportMode: TransportMode | null;
    fastestTravelSeconds: number | null;
    fastestDistanceMeters: number | null;
    fastestTransportMode: TransportMode | null;
}

export interface PageResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
    last: boolean;
}

export const HELP_REQUEST_TYPE_LABELS: Record<HelpRequestType, string> = {
    FOOD: "🍎 Alimentación", TRANSPORT: "🚗 Transporte",
    COMPANIONSHIP: "🤝 Acompañamiento", MEDICAL: "🏥 Médico",
    EDUCATION: "📚 Educación", PET_CARE: "🐾 Mascotas",
    BABY_CARE: "👶 Bebés", COMMUNITY_EVENTS: "🎉 Eventos",
    EMERGENCY: "🚨 Emergencia", SHOPPING: "🛒 Compras", OTHER: "📌 Otro",
};

export const HELP_REQUEST_STATUS_LABELS: Record<HelpRequestStatus, string> = {
    OPEN: "Abierta",
    ACCEPTED: "Aceptada",
    COMPLETED: "Completada",
    CANCELLED: "Cancelada",
    EXPIRED: "Expirada",
};

export const HELP_REQUEST_STATUS_COLORS: Record<HelpRequestStatus, string> = {
    OPEN: "#28a745",
    ACCEPTED: "#667eea",
    COMPLETED: "#6c757d",
    CANCELLED: "#dc3545",
    EXPIRED: "#fd7e14",
};