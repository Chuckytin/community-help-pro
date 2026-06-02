import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { toast } from "react-toastify";
import Menubar from "../../components/Menubar";
import { useAppContext } from "../../hooks/useAppContext";
import {
    HelpRequest, HelpRequestStatus, PageResponse,
    HELP_REQUEST_TYPE_LABELS, HELP_REQUEST_STATUS_LABELS, HELP_REQUEST_STATUS_COLORS
} from "../../types/helpRequest";

const MyHelpRequests = () => {
    const navigate = useNavigate();
    const { backendURL } = useAppContext();

    const [requests, setRequests] = useState<HelpRequest[]>([]);
    const [loading, setLoading] = useState(true);
    const [statusFilter, setStatusFilter] = useState<HelpRequestStatus | "">("");
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    useEffect(() => {
        const fetch = async () => {
            setLoading(true);
            try {
                const { data } = await axios.get<PageResponse<HelpRequest>>(
                    `${backendURL}/help-requests/me`, {
                        params: {
                            page,
                            size: 10,
                            ...(statusFilter ? { status: statusFilter } : {})
                        }
                    }
                );
                setRequests(data.content);
                setTotalPages(data.totalPages);
            } catch {
                toast.error("Error al cargar tus solicitudes.");
            } finally {
                setLoading(false);
            }
        };
        fetch();
    }, [page, statusFilter, backendURL]);

    const handleDelete = async (id: string) => {
        if (!confirm("¿Eliminar esta solicitud?")) return;
        try {
            await axios.delete(`${backendURL}/help-requests/${id}`);
            toast.success("Solicitud eliminada.");
            setRequests(r => r.filter(req => req.id !== id));
        } catch {
            toast.error("No se pudo eliminar la solicitud.");
        }
    };

    const handleCancel = async (id: string) => {
        if (!confirm("¿Cancelar esta solicitud?")) return;
        try {
            const { data } = await axios.post<HelpRequest>(
                `${backendURL}/help-requests/${id}/cancel`
            );
            setRequests(r => r.map(req => req.id === id ? data : req));
            toast.success("Solicitud cancelada.");
        } catch {
            toast.error("No se pudo cancelar la solicitud.");
        }
    };

    const formatDate = (s: string) =>
        new Date(s).toLocaleDateString("es-ES", { day: "2-digit", month: "short", year: "numeric" });

    return (
        <div className="d-flex flex-column min-vh-100" style={{ background: "#f8f9fa" }}>
            <Menubar />
            <div className="container py-4">

                {/* Cabecera */}
                <div className="d-flex justify-content-between align-items-center mb-4">
                    <h4 className="fw-bold mb-0">📋 Mis solicitudes de ayuda</h4>
                    <button className="btn px-3" style={{
                        background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
                        color: "white", border: "none", borderRadius: 8, fontWeight: 600
                    }} onClick={() => navigate("/help-requests/new")}>
                        + Nueva solicitud
                    </button>
                </div>

                {/* Filtro por estado */}
                <div className="d-flex gap-2 mb-4 flex-wrap">
                    {(["", "OPEN", "COMPLETED", "CANCELLED", "EXPIRED"] as (HelpRequestStatus | "")[]).map(s => (
                        <button key={s} className="btn btn-sm"
                            style={{
                                borderRadius: 20,
                                border: statusFilter === s ? "2px solid #667eea" : "1px solid #e2e8f0",
                                background: statusFilter === s ? "rgba(102,126,234,0.1)" : "white",
                                color: statusFilter === s ? "#667eea" : "#4a5568",
                                fontWeight: statusFilter === s ? 600 : 400,
                            }}
                            onClick={() => { setStatusFilter(s); setPage(0); }}>
                            {s === "" ? "Todas" : HELP_REQUEST_STATUS_LABELS[s]}
                        </button>
                    ))}
                </div>

                {/* Lista */}
                {loading ? (
                    <div className="text-center py-5">
                        <div className="spinner-border" style={{ color: "#667eea" }} />
                    </div>
                ) : requests.length === 0 ? (
                    <div className="text-center py-5 text-muted">
                        <span style={{ fontSize: "3rem" }}>📋</span>
                        <p className="mt-3 mb-2">No tienes solicitudes {statusFilter ? "con este estado" : "todavía"}</p>
                        <button className="btn btn-sm" style={{
                            background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
                            color: "white", border: "none", borderRadius: 20
                        }} onClick={() => navigate("/help-requests/new")}>
                            Crear tu primera solicitud
                        </button>
                    </div>
                ) : (
                    <div className="d-flex flex-column gap-3">
                        {requests.map(r => (
                            <div key={r.id} className="card shadow-sm"
                                style={{ borderRadius: 12, borderLeft: `4px solid ${HELP_REQUEST_STATUS_COLORS[r.status]}` }}>
                                <div className="card-body">
                                    <div className="d-flex justify-content-between align-items-start">
                                        <div className="grow" style={{ cursor: "pointer" }}
                                            onClick={() => navigate(`/help-requests/${r.id}`)}>
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
                                            <p className="text-muted small mb-1"
                                                style={{
                                                    overflow: "hidden", display: "-webkit-box",
                                                    WebkitLineClamp: 2, WebkitBoxOrient: "vertical"
                                                } as React.CSSProperties}>
                                                {r.description}
                                            </p>
                                            <span className="text-muted" style={{ fontSize: "0.75rem" }}>
                                                Creada el {formatDate(r.createdAt)}
                                                {r.deadline && ` · Límite: ${formatDate(r.deadline)}`}
                                            </span>
                                        </div>

                                        {/* Acciones */}
                                        <div className="d-flex gap-1 ms-3">
                                            {r.status === "OPEN" && (
                                                <>
                                                    <button className="btn btn-sm btn-outline-secondary"
                                                        style={{ borderRadius: 8 }}
                                                        onClick={() => navigate(`/help-requests/${r.id}/edit`)}>
                                                        ✏️
                                                    </button>
                                                    <button className="btn btn-sm btn-outline-warning"
                                                        style={{ borderRadius: 8 }}
                                                        onClick={() => handleCancel(r.id)}>
                                                        ✕
                                                    </button>
                                                </>
                                            )}
                                            {(r.status === "COMPLETED" || r.status === "CANCELLED" || r.status === "EXPIRED") && (
                                                <button className="btn btn-sm btn-outline-danger"
                                                    style={{ borderRadius: 8 }}
                                                    onClick={() => handleDelete(r.id)}>
                                                    🗑️
                                                </button>
                                            )}
                                        </div>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                )}

                {/* Paginación */}
                {totalPages > 1 && (
                    <div className="d-flex justify-content-center gap-2 mt-4">
                        <button className="btn btn-sm btn-outline-secondary"
                            disabled={page === 0}
                            onClick={() => setPage(p => p - 1)}>
                            ← Anterior
                        </button>
                        <span className="btn btn-sm disabled bg-white">
                            {page + 1} / {totalPages}
                        </span>
                        <button className="btn btn-sm btn-outline-secondary"
                            disabled={page >= totalPages - 1}
                            onClick={() => setPage(p => p + 1)}>
                            Siguiente →
                        </button>
                    </div>
                )}
            </div>
        </div>
    );
};

export default MyHelpRequests;