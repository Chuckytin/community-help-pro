import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import axios from "axios";
import { toast } from "react-toastify";
import { MapContainer, TileLayer, Marker, Popup } from "react-leaflet";
import L from "leaflet";
import Menubar from "../../components/Menubar";
import { useAppContext } from "../../hooks/useAppContext";
import {
    Donation,
    DONATION_TYPE_LABELS, FOOD_TYPE_LABELS,
    DONATION_STATUS_LABELS, DONATION_STATUS_COLORS,
} from "../../types/donation";

// eslint-disable-next-line @typescript-eslint/no-explicit-any
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
    iconRetinaUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png",
    iconUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png",
    shadowUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png",
});

/**
 * Vista detallada de una donación.
 * Ciclo de vida: AVAILABLE → RESERVED → CONFIRMED → PICKED_UP → COMPLETED
 *
 * Acciones según rol:
 * - Donante: confirmar reserva, cancelar si AVAILABLE/RESERVED
 * - Voluntario: reservar si AVAILABLE, marcar recogida, completar
 */
const DonationDetail = () => {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const { backendURL, userData } = useAppContext();

    const [donation, setDonation] = useState<Donation | null>(null);
    const [loading, setLoading] = useState(true);
    const [actionLoading, setActionLoading] = useState(false);

    useEffect(() => {
        const fetch = async () => {
            setLoading(true);
            try {
                const { data } = await axios.get<Donation>(
                    `${backendURL}/donations/${id}`
                );
                setDonation(data);
            } catch {
                toast.error("No se pudo cargar la donación.");
                navigate(-1);
            } finally {
                setLoading(false);
            }
        };
        if (id) fetch();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [id, backendURL]);

    const isDonor = donation?.donorId === userData?.id;
    const isAssignedVolunteer = donation?.volunteerId === userData?.id;

    const doAction = async (endpoint: string, successMsg: string) => {
        setActionLoading(true);
        try {
            const { data } = await axios.post<Donation>(
                `${backendURL}/donations/${id}/${endpoint}`
            );
            setDonation(data);
            toast.success(successMsg);
        } catch (error: unknown) {
            if (axios.isAxiosError(error)) {
                toast.error(error.response?.data?.message || "Error en la acción.");
            }
        } finally {
            setActionLoading(false);
        }
    };

    const handleCancel = async () => {
        if (!confirm("¿Cancelar esta donación?")) return;
        doAction("cancel", "Donación cancelada.");
    };

    const formatDate = (s: string) =>
        new Date(s).toLocaleDateString("es-ES", {
            day: "2-digit", month: "long", year: "numeric",
            hour: "2-digit", minute: "2-digit"
        });

    const formatTime = (s: number) => {
        const m = Math.round(s / 60);
        return m < 60 ? `${m} min` : `${Math.floor(m / 60)}h ${m % 60}min`;
    };

    const formatDist = (m: number) =>
        m < 1000 ? `${Math.round(m)} m` : `${(m / 1000).toFixed(1)} km`;

    if (loading) return (
        <div className="d-flex flex-column min-vh-100" style={{ background: "#f8f9fa" }}>
            <Menubar />
            <div className="d-flex justify-content-center align-items-center grow">
                <div className="spinner-border" style={{ color: "#28a745" }} />
            </div>
        </div>
    );

    if (!donation) return null;

    const statusColor = DONATION_STATUS_COLORS[donation.status];

    /** Timeline del ciclo de vida de la donación */
    const timeline = [
        { status: "AVAILABLE", label: "Disponible", icon: "📦" },
        { status: "RESERVED", label: "Reservada", icon: "🔒" },
        { status: "CONFIRMED", label: "Confirmada", icon: "✅" },
        { status: "PICKED_UP", label: "Recogida", icon: "🚗" },
        { status: "COMPLETED", label: "Completada", icon: "🎉" },
    ];
    const timelineIndex = timeline.findIndex(t => t.status === donation.status);

    return (
        <div className="d-flex flex-column min-vh-100" style={{ background: "#f8f9fa" }}>
            <Menubar />
            <div className="container py-4" style={{ maxWidth: 760 }}>

                <button className="btn btn-sm btn-outline-secondary rounded-pill mb-4"
                    onClick={() => navigate(-1)}>
                    ← Volver
                </button>

                <div className="card shadow-sm" style={{ borderRadius: 12, borderTop: `4px solid ${statusColor}` }}>
                    <div className="card-body p-4">

                        {/* Cabecera */}
                        <div className="d-flex justify-content-between align-items-start mb-3">
                            <div>
                                <div className="d-flex align-items-center gap-2 mb-2">
                                    <span className="badge" style={{
                                        background: `${statusColor}20`,
                                        color: statusColor, borderRadius: 20, fontSize: "0.8rem"
                                    }}>
                                        {DONATION_STATUS_LABELS[donation.status]}
                                    </span>
                                    <span className="text-muted small">
                                        {DONATION_TYPE_LABELS[donation.donationType]}
                                        {donation.foodType && ` · ${FOOD_TYPE_LABELS[donation.foodType]}`}
                                    </span>
                                    {donation.quantity && donation.unit && (
                                        <span className="text-muted small">
                                            · {donation.quantity} {donation.unit}
                                        </span>
                                    )}
                                </div>
                                <h3 className="fw-bold mb-0">{donation.title}</h3>
                            </div>

                            {isDonor && donation.status === "AVAILABLE" && (
                                <div className="d-flex gap-2">
                                    <button className="btn btn-sm btn-outline-secondary"
                                        style={{ borderRadius: 8 }}
                                        onClick={() => navigate(`/donations/${id}/edit`)}>
                                        ✏️ Editar
                                    </button>
                                    <button className="btn btn-sm btn-outline-danger"
                                        style={{ borderRadius: 8 }}
                                        disabled={actionLoading}
                                        onClick={handleCancel}>
                                        Cancelar
                                    </button>
                                </div>
                            )}
                        </div>

                        {/* Descripción */}
                        <p className="text-muted mb-4" style={{ lineHeight: 1.7 }}>
                            {donation.description}
                        </p>

                        {/* Timeline del ciclo de vida */}
                        {donation.status !== "CANCELLED" && donation.status !== "EXPIRED" && (
                            <div className="mb-4">
                                <div className="text-muted small mb-2 fw-medium">Estado del proceso</div>
                                <div className="d-flex align-items-center gap-0">
                                    {timeline.map((step, idx) => (
                                        <div key={step.status} className="d-flex align-items-center grow">
                                            <div className="text-center" style={{ minWidth: 60 }}>
                                                <div style={{
                                                    width: 36, height: 36, borderRadius: "50%",
                                                    margin: "0 auto",
                                                    background: idx <= timelineIndex
                                                        ? "linear-gradient(135deg, #28a745 0%, #20c997 100%)"
                                                        : "#e9ecef",
                                                    display: "flex", alignItems: "center",
                                                    justifyContent: "center", fontSize: "1rem"
                                                }}>
                                                    {idx <= timelineIndex ? step.icon : "○"}
                                                </div>
                                                <div className="mt-1" style={{
                                                    fontSize: "0.65rem",
                                                    color: idx <= timelineIndex ? "#28a745" : "#adb5bd",
                                                    fontWeight: idx === timelineIndex ? 700 : 400
                                                }}>
                                                    {step.label}
                                                </div>
                                            </div>
                                            {idx < timeline.length - 1 && (
                                                <div style={{
                                                    flexGrow: 1, height: 3,
                                                    background: idx < timelineIndex ? "#28a745" : "#e9ecef",
                                                    margin: "0 2px", marginTop: -16
                                                }} />
                                            )}
                                        </div>
                                    ))}
                                </div>
                            </div>
                        )}

                        {/* Fechas */}
                        <div className="row g-3 mb-4">
                            <div className="col-6 col-md-3">
                                <div className="text-muted small">Publicada</div>
                                <div className="fw-medium small">{formatDate(donation.createdAt)}</div>
                            </div>
                            {donation.expiryDate && (
                                <div className="col-6 col-md-3">
                                    <div className="text-muted small">Caduca</div>
                                    <div className="fw-medium small">{formatDate(donation.expiryDate)}</div>
                                </div>
                            )}
                            {donation.reservedAt && (
                                <div className="col-6 col-md-3">
                                    <div className="text-muted small">Reservada</div>
                                    <div className="fw-medium small">{formatDate(donation.reservedAt)}</div>
                                </div>
                            )}
                            {donation.completedAt && (
                                <div className="col-6 col-md-3">
                                    <div className="text-muted small">Completada</div>
                                    <div className="fw-medium small">{formatDate(donation.completedAt)}</div>
                                </div>
                            )}
                        </div>

                        {/* Estimación de viaje */}
                        {!isDonor && donation.estimatedDistanceMeters && (
                            <div className="alert py-2 px-3 mb-4 d-flex gap-4"
                                style={{
                                    background: "rgba(40,167,69,0.08)",
                                    border: "1px solid rgba(40,167,69,0.2)",
                                    borderRadius: 10
                                }}>
                                <div>
                                    <div className="text-muted small">Distancia</div>
                                    <div className="fw-semibold">
                                        📍 {formatDist(donation.estimatedDistanceMeters)}
                                    </div>
                                </div>
                                {donation.estimatedTravelSeconds && (
                                    <div>
                                        <div className="text-muted small">Tiempo estimado</div>
                                        <div className="fw-semibold">
                                            ⏱ {formatTime(donation.estimatedTravelSeconds)}
                                        </div>
                                    </div>
                                )}
                            </div>
                        )}

                        {/* Mapa */}
                        {donation.latitude && donation.longitude && (
                            <div className="mb-4">
                                <div className="text-muted small mb-2 fw-medium">📍 Ubicación</div>
                                <MapContainer
                                    center={[donation.latitude, donation.longitude]}
                                    zoom={15}
                                    style={{ height: 220, borderRadius: 10 }}
                                    scrollWheelZoom={false}
                                >
                                    <TileLayer
                                        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                                        attribution='&copy; OpenStreetMap'
                                    />
                                    <Marker position={[donation.latitude, donation.longitude]}>
                                        <Popup>{donation.title}</Popup>
                                    </Marker>
                                </MapContainer>
                            </div>
                        )}

                        {/* Acciones según rol y estado */}
                        <div className="d-flex gap-2 flex-wrap">
                            {/* Voluntario puede reservar */}
                            {!isDonor && donation.status === "AVAILABLE" && (
                                <button className="btn px-4" style={{
                                    background: "linear-gradient(135deg, #28a745 0%, #20c997 100%)",
                                    color: "white", border: "none", borderRadius: 8, fontWeight: 600
                                }} disabled={actionLoading}
                                    onClick={() => doAction("reserve", "¡Donación reservada!")}>
                                    {actionLoading && <span className="spinner-border spinner-border-sm me-2" />}
                                    🔒 Reservar donación
                                </button>
                            )}

                            {/* Donante confirma la reserva */}
                            {isDonor && donation.status === "RESERVED" && (
                                <button className="btn px-4" style={{
                                    background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
                                    color: "white", border: "none", borderRadius: 8, fontWeight: 600
                                }} disabled={actionLoading}
                                    onClick={() => doAction("confirm", "¡Reserva confirmada!")}>
                                    {actionLoading && <span className="spinner-border spinner-border-sm me-2" />}
                                    ✅ Confirmar reserva
                                </button>
                            )}

                            {/* Voluntario marca recogida */}
                            {isAssignedVolunteer && donation.status === "CONFIRMED" && (
                                <button className="btn px-4" style={{
                                    background: "linear-gradient(135deg, #fd7e14 0%, #ffc107 100%)",
                                    color: "white", border: "none", borderRadius: 8, fontWeight: 600
                                }} disabled={actionLoading}
                                    onClick={() => doAction("pickup", "¡Recogida registrada!")}>
                                    {actionLoading && <span className="spinner-border spinner-border-sm me-2" />}
                                    🚗 Marcar como recogida
                                </button>
                            )}

                            {/* Voluntario completa */}
                            {isAssignedVolunteer && donation.status === "PICKED_UP" && (
                                <button className="btn px-4" style={{
                                    background: "linear-gradient(135deg, #28a745 0%, #20c997 100%)",
                                    color: "white", border: "none", borderRadius: 8, fontWeight: 600
                                }} disabled={actionLoading}
                                    onClick={() => doAction("complete", "¡Donación completada! Gracias.")}>
                                    {actionLoading && <span className="spinner-border spinner-border-sm me-2" />}
                                    🎉 Marcar como completada
                                </button>
                            )}

                            {/* Donante cancela si está reservada */}
                            {isDonor && donation.status === "RESERVED" && (
                                <button className="btn btn-outline-danger"
                                    style={{ borderRadius: 8 }}
                                    disabled={actionLoading}
                                    onClick={handleCancel}>
                                    Cancelar donación
                                </button>
                            )}

                            {/* Si la donación está completada, se puede dejar una reseña */}
                            {donation.status === "COMPLETED" && (
                                <button className="btn btn-sm" style={{
                                    background: "rgba(102,126,234,0.1)",
                                    border: "1px solid rgba(102,126,234,0.3)",
                                    color: "#667eea", borderRadius: 8
                                }} onClick={() => {
                                    // Si soy el donante, valoro al voluntario
                                    // Si soy el voluntario, valoro al donante
                                    const targetId = isDonor ? donation.volunteerId : donation.donorId;
                                    navigate(`/reviews/new?targetId=${targetId}&donationId=${donation.id}`);
                                }}>
                                    ⭐ Dejar reseña
                                </button>
                            )}
                        </div>

                        {donation.cancelReason && (
                            <div className="alert alert-warning py-2 px-3 mt-3 small">
                                Motivo de cancelación: {donation.cancelReason}
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default DonationDetail;