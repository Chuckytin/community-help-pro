import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { toast } from "react-toastify";
import Menubar from "../../components/Menubar";
import { useAppContext } from "../../hooks/useAppContext";
import { HelpRequest, PageResponse as HRPage, HELP_REQUEST_TYPE_LABELS, HELP_REQUEST_STATUS_LABELS, HELP_REQUEST_STATUS_COLORS } from "../../types/helpRequest";
import { Donation, PageResponse as DPage, DONATION_TYPE_LABELS, DONATION_STATUS_LABELS, DONATION_STATUS_COLORS } from "../../types/donation";
import { Proposal, PROPOSAL_STATUS_LABELS, PROPOSAL_STATUS_COLORS } from "../../types/proposal";

type Tab = "proposals" | "help-requests" | "donations";

/**
 * Panel del voluntario con tres pestañas:
 * - Propuestas: las que el motor de matching envió al voluntario (PENDING)
 * - Solicitudes asignadas: HelpRequests que aceptó y tiene en curso
 * - Donaciones asignadas: Donations que reservó y tiene en curso
 */
const VolunteerAssigned = () => {
    const navigate = useNavigate();
    const { backendURL } = useAppContext();

    const [activeTab, setActiveTab] = useState<Tab>("proposals");

    // Propuestas
    const [proposals, setProposals] = useState<Proposal[]>([]);
    const [loadingProposals, setLoadingProposals] = useState(true);
    const [proposalPage, setProposalPage] = useState(0);
    const [proposalTotalPages, setProposalTotalPages] = useState(0);

    // HelpRequests asignadas
    const [assignedRequests, setAssignedRequests] = useState<HelpRequest[]>([]);
    const [loadingRequests, setLoadingRequests] = useState(false);
    const [requestPage, setRequestPage] = useState(0);
    const [requestTotalPages, setRequestTotalPages] = useState(0);

    // Donations asignadas
    const [assignedDonations, setAssignedDonations] = useState<Donation[]>([]);
    const [loadingDonations, setLoadingDonations] = useState(false);
    const [donationPage, setDonationPage] = useState(0);
    const [donationTotalPages, setDonationTotalPages] = useState(0);

    const [actionLoading, setActionLoading] = useState<string | null>(null);

    // Carga propuestas
    useEffect(() => {
        const fetch = async () => {
            setLoadingProposals(true);
            try {
                const { data } = await axios.get<{ content: Proposal[]; totalPages: number }>(
                    `${backendURL}/proposals/volunteer`,
                    { params: { page: proposalPage, size: 10 } }
                );
                setProposals(data.content);
                setProposalTotalPages(data.totalPages);
            } catch {
                toast.error("Error al cargar las propuestas.");
            } finally {
                setLoadingProposals(false);
            }
        };
        if (activeTab === "proposals") fetch();
    }, [activeTab, proposalPage, backendURL]);

    // Carga HelpRequests asignadas
    useEffect(() => {
        const fetch = async () => {
            setLoadingRequests(true);
            try {
                const { data } = await axios.get<HRPage<HelpRequest>>(
                    `${backendURL}/help-requests/assigned/me`,
                    { params: { page: requestPage, size: 10 } }
                );
                setAssignedRequests(data.content);
                setRequestTotalPages(data.totalPages);
            } catch {
                toast.error("Error al cargar las solicitudes asignadas.");
            } finally {
                setLoadingRequests(false);
            }
        };
        if (activeTab === "help-requests") fetch();
    }, [activeTab, requestPage, backendURL]);

    // Carga Donations asignadas
    useEffect(() => {
        const fetch = async () => {
            setLoadingDonations(true);
            try {
                const { data } = await axios.get<DPage<Donation>>(
                    `${backendURL}/donations/assigned/me`,
                    { params: { page: donationPage, size: 10 } }
                );
                setAssignedDonations(data.content);
                setDonationTotalPages(data.totalPages);
            } catch {
                toast.error("Error al cargar las donaciones asignadas.");
            } finally {
                setLoadingDonations(false);
            }
        };
        if (activeTab === "donations") fetch();
    }, [activeTab, donationPage, backendURL]);

    /** Acepta o rechaza una propuesta */
    const handleProposalAction = async (
        proposalId: string,
        action: "accept" | "reject"
    ) => {
        setActionLoading(proposalId);
        try {
            const { data } = await axios.post<Proposal>(
                `${backendURL}/proposals/${proposalId}/${action}`
            );
            // Actualiza la propuesta en la lista
            setProposals(prev =>
                prev.map(p => p.id === proposalId ? data : p)
            );
            if (action === "accept") {
                toast.success("¡Propuesta aceptada! Ya puedes coordinarte con el solicitante.");
            } else {
                toast.success("Propuesta rechazada.");
            }
        } catch (error: unknown) {
            if (axios.isAxiosError(error)) {
                // 409 = conflicto optimista (otro voluntario aceptó antes)
                if (error.response?.status === 409) {
                    toast.error("Esta propuesta ya fue asignada a otro voluntario.");
                } else {
                    toast.error(error.response?.data?.message || "Error en la acción.");
                }
            }
        } finally {
            setActionLoading(null);
        }
    };

    const formatDate = (s: string) =>
        new Date(s).toLocaleDateString("es-ES", {
            day: "2-digit", month: "short", year: "numeric",
            hour: "2-digit", minute: "2-digit"
        });

    const pendingProposals = proposals.filter(p => p.status === "PENDING").length;

    // Estilo de pestaña activa
    const tabStyle = (tab: Tab) => ({
        borderRadius: "8px 8px 0 0",
        border: "none",
        borderBottom: activeTab === tab ? "3px solid #667eea" : "3px solid transparent",
        background: "transparent",
        color: activeTab === tab ? "#667eea" : "#4a5568",
        fontWeight: activeTab === tab ? 700 : 400,
        padding: "10px 16px",
        fontSize: "0.9rem",
    } as React.CSSProperties);

    return (
        <div className="d-flex flex-column min-vh-100" style={{ background: "#f8f9fa" }}>
            <Menubar />
            <div className="container py-4">

                <h4 className="fw-bold mb-4">✅ Panel de voluntario</h4>

                {/* Pestañas */}
                <div className="d-flex gap-1 border-bottom mb-4">
                    <button style={tabStyle("proposals")} onClick={() => setActiveTab("proposals")}>
                        🔔 Propuestas
                        {pendingProposals > 0 && (
                            <span className="badge ms-2 rounded-pill"
                                style={{
                                    background: "#fd7e14",
                                    color: "white", fontSize: "0.7rem"
                                }}>
                                {pendingProposals}
                            </span>
                        )}
                    </button>
                    <button style={tabStyle("help-requests")} onClick={() => setActiveTab("help-requests")}>
                        🙋 Solicitudes asignadas
                    </button>
                    <button style={tabStyle("donations")} onClick={() => setActiveTab("donations")}>
                        🎁 Donaciones asignadas
                    </button>
                </div>

                {/* ── PESTAÑA: PROPUESTAS ── */}
                {activeTab === "proposals" && (
                    <>
                        {loadingProposals ? (
                            <div className="text-center py-5">
                                <div className="spinner-border" style={{ color: "#667eea" }} />
                            </div>
                        ) : proposals.length === 0 ? (
                            <div className="text-center py-5 text-muted">
                                <span style={{ fontSize: "3rem" }}>🔔</span>
                                <p className="mt-3">No tienes propuestas todavía.</p>
                                <p className="small">
                                    El motor de matching te enviará propuestas automáticamente
                                    cuando haya solicitudes o donaciones cercanas.
                                </p>
                                <button className="btn btn-sm mt-2" style={{
                                    background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
                                    color: "white", border: "none", borderRadius: 20
                                }} onClick={() => navigate("/profile/volunteer")}>
                                    Configurar perfil de voluntario
                                </button>
                            </div>
                        ) : (
                            <div className="d-flex flex-column gap-3">
                                {proposals.map(p => (
                                    <div key={p.id} className="card shadow-sm"
                                        style={{
                                            borderRadius: 12,
                                            borderLeft: `4px solid ${PROPOSAL_STATUS_COLORS[p.status]}`
                                        }}>
                                        <div className="card-body">
                                            <div className="d-flex justify-content-between align-items-start">
                                                <div className="grow">
                                                    <div className="d-flex align-items-center gap-2 mb-2">
                                                        <span className="badge" style={{
                                                            background: `${PROPOSAL_STATUS_COLORS[p.status]}20`,
                                                            color: PROPOSAL_STATUS_COLORS[p.status],
                                                            borderRadius: 20, fontSize: "0.7rem"
                                                        }}>
                                                            {PROPOSAL_STATUS_LABELS[p.status]}
                                                        </span>
                                                        <span className="text-muted small">
                                                            {p.type === "HELP_REQUEST" ? "🙋 Solicitud" : "🎁 Donación"}
                                                        </span>
                                                        {p.score !== null && (
                                                            <span className="text-muted small">
                                                                · Score: {(p.score * 100).toFixed(0)}%
                                                            </span>
                                                        )}
                                                    </div>

                                                    <p className="text-muted small mb-2">
                                                        Recibida el {formatDate(p.createdAt)}
                                                        {p.respondedAt && ` · Respondida: ${formatDate(p.respondedAt)}`}
                                                    </p>

                                                    {/* Botón para ver la entidad */}
                                                    <button className="btn btn-sm btn-outline-secondary"
                                                        style={{ borderRadius: 8, fontSize: "0.8rem" }}
                                                        onClick={() => navigate(
                                                            p.type === "HELP_REQUEST"
                                                                ? `/help-requests/${p.targetEntityId}`
                                                                : `/donations/${p.targetEntityId}`
                                                        )}>
                                                        Ver {p.type === "HELP_REQUEST" ? "solicitud" : "donación"} →
                                                    </button>
                                                </div>

                                                {/* Botones de acción — solo para PENDING */}
                                                {p.status === "PENDING" && (
                                                    <div className="d-flex gap-2 ms-3">
                                                        <button className="btn btn-sm px-3"
                                                            style={{
                                                                background: "linear-gradient(135deg, #28a745 0%, #20c997 100%)",
                                                                color: "white", border: "none",
                                                                borderRadius: 8, fontWeight: 600
                                                            }}
                                                            disabled={actionLoading === p.id}
                                                            onClick={() => handleProposalAction(p.id, "accept")}>
                                                            {actionLoading === p.id
                                                                ? <span className="spinner-border spinner-border-sm" />
                                                                : "✓ Aceptar"}
                                                        </button>
                                                        <button className="btn btn-sm btn-outline-danger px-3"
                                                            style={{ borderRadius: 8 }}
                                                            disabled={actionLoading === p.id}
                                                            onClick={() => handleProposalAction(p.id, "reject")}>
                                                            ✕ Rechazar
                                                        </button>
                                                    </div>
                                                )}
                                            </div>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}

                        {proposalTotalPages > 1 && (
                            <div className="d-flex justify-content-center gap-2 mt-4">
                                <button className="btn btn-sm btn-outline-secondary"
                                    disabled={proposalPage === 0}
                                    onClick={() => setProposalPage(p => p - 1)}>
                                    ← Anterior
                                </button>
                                <span className="btn btn-sm disabled bg-white">
                                    {proposalPage + 1} / {proposalTotalPages}
                                </span>
                                <button className="btn btn-sm btn-outline-secondary"
                                    disabled={proposalPage >= proposalTotalPages - 1}
                                    onClick={() => setProposalPage(p => p + 1)}>
                                    Siguiente →
                                </button>
                            </div>
                        )}
                    </>
                )}

                {/* ── PESTAÑA: SOLICITUDES ASIGNADAS ── */}
                {activeTab === "help-requests" && (
                    <>
                        {loadingRequests ? (
                            <div className="text-center py-5">
                                <div className="spinner-border" style={{ color: "#667eea" }} />
                            </div>
                        ) : assignedRequests.length === 0 ? (
                            <div className="text-center py-5 text-muted">
                                <span style={{ fontSize: "3rem" }}>🙋</span>
                                <p className="mt-3">No tienes solicitudes asignadas.</p>
                            </div>
                        ) : (
                            <div className="d-flex flex-column gap-3">
                                {assignedRequests.map(r => (
                                    <div key={r.id} className="card shadow-sm"
                                        style={{
                                            borderRadius: 12,
                                            borderLeft: `4px solid ${HELP_REQUEST_STATUS_COLORS[r.status]}`,
                                            cursor: "pointer"
                                        }}
                                        onClick={() => navigate(`/help-requests/${r.id}`)}>
                                        <div className="card-body">
                                            <div className="d-flex align-items-center gap-2 mb-1">
                                                <span className="badge" style={{
                                                    background: `${HELP_REQUEST_STATUS_COLORS[r.status]}20`,
                                                    color: HELP_REQUEST_STATUS_COLORS[r.status],
                                                    borderRadius: 20, fontSize: "0.7rem"
                                                }}>
                                                    {HELP_REQUEST_STATUS_LABELS[r.status]}
                                                </span>
                                                <span className="text-muted small">
                                                    {HELP_REQUEST_TYPE_LABELS[r.type]}
                                                </span>
                                            </div>
                                            <h6 className="fw-semibold mb-1">{r.title}</h6>
                                            <p className="text-muted small mb-0"
                                                style={{
                                                    overflow: "hidden", display: "-webkit-box",
                                                    WebkitLineClamp: 2, WebkitBoxOrient: "vertical"
                                                } as React.CSSProperties}>
                                                {r.description}
                                            </p>
                                            {r.acceptedAt && (
                                                <span className="text-muted mt-1 d-block"
                                                    style={{ fontSize: "0.75rem" }}>
                                                    Aceptada el {formatDate(r.acceptedAt)}
                                                </span>
                                            )}
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}

                        {requestTotalPages > 1 && (
                            <div className="d-flex justify-content-center gap-2 mt-4">
                                <button className="btn btn-sm btn-outline-secondary"
                                    disabled={requestPage === 0}
                                    onClick={() => setRequestPage(p => p - 1)}>
                                    ← Anterior
                                </button>
                                <span className="btn btn-sm disabled bg-white">
                                    {requestPage + 1} / {requestTotalPages}
                                </span>
                                <button className="btn btn-sm btn-outline-secondary"
                                    disabled={requestPage >= requestTotalPages - 1}
                                    onClick={() => setRequestPage(p => p + 1)}>
                                    Siguiente →
                                </button>
                            </div>
                        )}
                    </>
                )}

                {/* ── PESTAÑA: DONACIONES ASIGNADAS ── */}
                {activeTab === "donations" && (
                    <>
                        {loadingDonations ? (
                            <div className="text-center py-5">
                                <div className="spinner-border" style={{ color: "#28a745" }} />
                            </div>
                        ) : assignedDonations.length === 0 ? (
                            <div className="text-center py-5 text-muted">
                                <span style={{ fontSize: "3rem" }}>🎁</span>
                                <p className="mt-3">No tienes donaciones asignadas.</p>
                            </div>
                        ) : (
                            <div className="d-flex flex-column gap-3">
                                {assignedDonations.map(d => (
                                    <div key={d.id} className="card shadow-sm"
                                        style={{
                                            borderRadius: 12,
                                            borderLeft: `4px solid ${DONATION_STATUS_COLORS[d.status]}`,
                                            cursor: "pointer"
                                        }}
                                        onClick={() => navigate(`/donations/${d.id}`)}>
                                        <div className="card-body">
                                            <div className="d-flex align-items-center gap-2 mb-1">
                                                <span className="badge" style={{
                                                    background: `${DONATION_STATUS_COLORS[d.status]}20`,
                                                    color: DONATION_STATUS_COLORS[d.status],
                                                    borderRadius: 20, fontSize: "0.7rem"
                                                }}>
                                                    {DONATION_STATUS_LABELS[d.status]}
                                                </span>
                                                <span className="text-muted small">
                                                    {DONATION_TYPE_LABELS[d.donationType]}
                                                    {d.quantity && d.unit && ` · ${d.quantity} ${d.unit}`}
                                                </span>
                                            </div>
                                            <h6 className="fw-semibold mb-1">{d.title}</h6>
                                            <p className="text-muted small mb-0"
                                                style={{
                                                    overflow: "hidden", display: "-webkit-box",
                                                    WebkitLineClamp: 2, WebkitBoxOrient: "vertical"
                                                } as React.CSSProperties}>
                                                {d.description}
                                            </p>
                                            {d.reservedAt && (
                                                <span className="text-muted mt-1 d-block"
                                                    style={{ fontSize: "0.75rem" }}>
                                                    Reservada el {formatDate(d.reservedAt)}
                                                </span>
                                            )}
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}

                        {donationTotalPages > 1 && (
                            <div className="d-flex justify-content-center gap-2 mt-4">
                                <button className="btn btn-sm btn-outline-secondary"
                                    disabled={donationPage === 0}
                                    onClick={() => setDonationPage(p => p - 1)}>
                                    ← Anterior
                                </button>
                                <span className="btn btn-sm disabled bg-white">
                                    {donationPage + 1} / {donationTotalPages}
                                </span>
                                <button className="btn btn-sm btn-outline-secondary"
                                    disabled={donationPage >= donationTotalPages - 1}
                                    onClick={() => setDonationPage(p => p + 1)}>
                                    Siguiente →
                                </button>
                            </div>
                        )}
                    </>
                )}
            </div>
        </div>
    );
};

export default VolunteerAssigned;