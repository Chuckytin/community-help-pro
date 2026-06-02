export type DonationStatus =
    | "AVAILABLE" | "RESERVED" | "CONFIRMED" | "PICKED_UP"
    | "COMPLETED" | "CANCELLED" | "EXPIRED";

export type DonationType =
    | "FOOD" | "CLOTHING" | "HYGIENE" | "TOYS" | "FURNITURE"
    | "ELECTRONICS" | "MEDICAL_SUPPLIES" | "BOOKS" | "STATIONERY"
    | "VEHICLES" | "COMMUNITY_EQUIPMENT" | "OTHER";

export type FoodType = "FRUIT" | "VEGETABLE" | "MEAT" | "DAIRY" | "COOKED" | "OTHER";
export type TransportMode = "FOOT_WALKING" | "CYCLING_REGULAR" | "DRIVING_CAR";

export interface Donation {
    id: string;
    donorId: string;
    volunteerId: string | null;
    donationType: DonationType;
    foodType: FoodType | null;
    title: string;
    description: string;
    quantity: number | null;
    unit: string | null;
    latitude: number | null;
    longitude: number | null;
    status: DonationStatus;
    active: boolean;
    cancelReason: string | null;
    createdAt: string;
    updatedAt: string;
    expiryDate: string | null;
    pickedUpAt: string | null;
    reservedAt: string | null;
    confirmedAt: string | null;
    completedAt: string | null;
    estimatedTravelSeconds: number | null;
    estimatedDistanceMeters: number | null;
    usedTransportMode: TransportMode | null;
    fastestTravelSeconds: number | null;
    fastestDistanceMeters: number | null;
    fastestTransportMode: TransportMode | null;
}

export const DONATION_TYPE_LABELS: Record<DonationType, string> = {
    FOOD: "🍎 Alimentos", CLOTHING: "👕 Ropa", HYGIENE: "🧴 Higiene",
    TOYS: "🧸 Juguetes", FURNITURE: "🪑 Muebles", ELECTRONICS: "💻 Electrónica",
    MEDICAL_SUPPLIES: "💊 Material médico", BOOKS: "📚 Libros",
    STATIONERY: "✏️ Papelería", VEHICLES: "🚗 Vehículos",
    COMMUNITY_EQUIPMENT: "🏗️ Equipamiento comunitario", OTHER: "📦 Otro",
};

export const FOOD_TYPE_LABELS: Record<FoodType, string> = {
    FRUIT: "🍎 Fruta", VEGETABLE: "🥦 Verdura", MEAT: "🥩 Carne",
    DAIRY: "🥛 Lácteos", COOKED: "🍲 Cocinado", OTHER: "📦 Otro",
};

export const DONATION_STATUS_LABELS: Record<DonationStatus, string> = {
    AVAILABLE: "Disponible", RESERVED: "Reservada", CONFIRMED: "Confirmada",
    PICKED_UP: "Recogida", COMPLETED: "Completada", CANCELLED: "Cancelada", EXPIRED: "Expirada",
};

export const DONATION_STATUS_COLORS: Record<DonationStatus, string> = {
    AVAILABLE: "#28a745", RESERVED: "#667eea", CONFIRMED: "#20c997",
    PICKED_UP: "#fd7e14", COMPLETED: "#6c757d", CANCELLED: "#dc3545", EXPIRED: "#adb5bd",
};