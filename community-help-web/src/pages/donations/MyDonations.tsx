import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { toast } from "react-toastify";
import Menubar from "../../components/Menubar";
import { useAppContext } from "../../hooks/useAppContext";
import {
    Donation, DonationStatus, PageResponse,
    DONATION_TYPE_LABELS, DONATION_STATUS_LABELS, DONATION_STATUS_COLORS
} from "../../types/donation";

const MyDonations = () => {
    const navigate = useNavigate();
    const { backendURL } = useAppContext();

    const [donations, setDonations] = useState<Donation[]>([]);
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    useEffect(() => {
        const fetch = async () => {
            setLoading(true);
            try {
                const { data } = await axios.get<PageResponse<Donation>>(
                    `${backendURL}/donations/me`, { params: { page, size: 10 } }
                );
                setDonations(data.content);
                setTotalPages(data.totalPages);
            } catch {
                toast.error("Error al cargar tus donaciones.");
            } finally {
                setLoading(false);
            }
        };
        fetch();
    }, [page, backendURL]);

    const handleDelete = async (id: string) => {
        if (!confirm("¿Eliminar esta donación?")) return;
        try {
            await axios.delete(`${backendURL}/donations/${id}`);
            toast.success("Donación eliminada.");
            setDonations(d => d.filter(don => don.id !== id));
        } catch {
            toast.error("No se pudo eliminar.");
        }
    };

    const handleCancel = async (id: string) => {
        if (!confirm("¿Cancelar esta donación?")) return;
        try {
            const { data } = await axios.post<Donation>(`${backendURL}/donations/${id}/cancel`);
            setDonations(d => d.map(don => don.id === id ? data : don));
            toast.success("Donación cancelada.");
        } catch {
            toast.error("No se pudo cancelar.");
        }
    };

    const formatDate = (s: string) =>
        new Date(s).toLocaleDateString("es-ES", { day: "2-digit", month: "short", year: "numeric" });

    return (
        <div className="d-flex flex-column min-vh-100" style={{ background: "#f8f9fa" }}>
            <Menubar />
            <div className="container py-4">

                <div className="d-flex justify-content-between align-items-center mb-4">
                    <h4 className="fw-bold mb-0">📦 Mis donaciones</h4>
                    <button className="btn px-3" style={{
                        background: "linear-gradient(135deg, #28a745 0%, #20c997 100%)",
                        color: "white", border: "none", borderRadius: 8, fontWeight: 600
                    }} onClick={() => navigate("/donations/new")}>
                        + Nueva donación
                    </button>
                </div>

                {loading ? (
                    <div className="text-center py-5">
                        <div className="spinner-border" style={{ color: "#28a745" }} />
                    </div>
                ) : donations.length === 0 ? (
                    <div className="text-center py-5 text-muted">
                        <span style={{ fontSize: "3rem" }}>📦</span>
                        <p className="mt-3 mb-2">No tienes donaciones todavía</p>
                        <button className="btn btn-sm" style={{
                            background: "linear-gradient(135deg, #28a745 0%, #20c997 100%)",
                            color: "white", border: "none", borderRadius: 20
                        }} onClick={() => navigate("/donations/new")}>
                            Publicar tu primera donación
                        </button>
                    </div>
                ) : (
                    <div className="d-flex flex-column gap-3">
                        {donations.map(d => (
                            <div key={d.id} className="card shadow-sm"
                                style={{ borderRadius: 12, borderLeft: `4px solid ${DONATION_STATUS_COLORS[d.status]}` }}>
                                <div className="card-body">
                                    <div className="d-flex justify-content-between align-items-start">
                                        <div className="grow" style={{ cursor: "pointer" }}
                                            onClick={() => navigate(`/donations/${d.id}`)}>
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
                                                </span>
                                                {d.quantity && d.unit && (
                                                    <span className="text-muted small">
                                                        · {d.quantity} {d.unit}
                                                    </span>
                                                )}
                                            </div>
                                            <h6 className="fw-semibold mb-1">{d.title}</h6>
                                            <p className="text-muted small mb-1"
                                                style={{
                                                    overflow: "hidden", display: "-webkit-box",
                                                    WebkitLineClamp: 2, WebkitBoxOrient: "vertical"
                                                } as React.CSSProperties}>
                                                {d.description}
                                            </p>
                                            <span className="text-muted" style={{ fontSize: "0.75rem" }}>
                                                Publicada el {formatDate(d.createdAt)}
                                                {d.expiryDate && ` · Caduca: ${formatDate(d.expiryDate)}`}
                                            </span>
                                        </div>

                                        <div className="d-flex gap-1 ms-3">
                                            {d.status === "AVAILABLE" && (
                                                <>
                                                    <button className="btn btn-sm btn-outline-secondary"
                                                        style={{ borderRadius: 8 }}
                                                        onClick={() => navigate(`/donations/${d.id}/edit`)}>
                                                        ✏️
                                                    </button>
                                                    <button className="btn btn-sm btn-outline-warning"
                                                        style={{ borderRadius: 8 }}
                                                        onClick={() => handleCancel(d.id)}>
                                                        ✕
                                                    </button>
                                                </>
                                            )}
                                            {d.status === "RESERVED" && (
                                                <button className="btn btn-sm" style={{
                                                    borderRadius: 8, background: "rgba(40,167,69,0.1)",
                                                    color: "#28a745", border: "1px solid #28a745", fontSize: "0.8rem"
                                                }} onClick={() => navigate(`/donations/${d.id}`)}>
                                                    Confirmar →
                                                </button>
                                            )}
                                            {(d.status === "COMPLETED" || d.status === "CANCELLED" || d.status === "EXPIRED") && (
                                                <button className="btn btn-sm btn-outline-danger"
                                                    style={{ borderRadius: 8 }}
                                                    onClick={() => handleDelete(d.id)}>
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

export default MyDonations;