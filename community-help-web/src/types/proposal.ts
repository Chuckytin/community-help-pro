export type ProposalStatus = "PENDING" | "ACCEPTED" | "REJECTED" | "CANCELLED" | "EXPIRED";
export type ProposalType = "DONATION" | "HELP_REQUEST";

export interface Proposal {
    id: string;
    type: ProposalType;
    targetEntityId: string;
    volunteerId: string;
    volunteerName: string;
    status: ProposalStatus;
    score: number | null;
    createdAt: string;
    updatedAt: string;
    respondedAt: string | null;
}

export const PROPOSAL_STATUS_LABELS: Record<ProposalStatus, string> = {
    PENDING: "Pendiente",
    ACCEPTED: "Aceptada",
    REJECTED: "Rechazada",
    CANCELLED: "Cancelada",
    EXPIRED: "Expirada",
};

export const PROPOSAL_STATUS_COLORS: Record<ProposalStatus, string> = {
    PENDING: "#fd7e14",
    ACCEPTED: "#28a745",
    REJECTED: "#dc3545",
    CANCELLED: "#6c757d",
    EXPIRED: "#adb5bd",
};