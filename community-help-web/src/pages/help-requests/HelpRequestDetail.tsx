import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import axios from "axios";
import { toast } from "react-toastify";
import { MapContainer, TileLayer, Marker, Popup } from "react-leaflet";
import L from "leaflet";
import Menubar from "../../components/Menubar";
import { useAppContext } from "../../hooks/useAppContext";
import {
    HelpRequest,
    HELP_REQUEST_TYPE_LABELS,
    HELP_REQUEST_STATUS_LABELS,
    HELP_REQUEST_STATUS_COLORS,
} from "../../types/helpRequest";

// Fix icono Leaflet
// eslint-disable-next-line @typescript-eslint/no-explicit-any
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
    iconRetinaUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png",
    iconUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png",
    shadowUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png",
});

/**
 * Vista detallada de una solicitud de ayuda.
 * - El creador puede editar, cancelar o ver el estado
 * - Un voluntario puede aceptarla si está OPEN
 * - Si está ASSIGNED/ACCEPTED, el voluntario puede completarla
 */
const HelpRequestDetail = () => {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const { backendURL, userData } = useAppContext();

    const [request, setRequest] = useState<HelpRequest | null>(null);
    const [loading, setLoading] = useState(true);
    const [actionLoading, setActionLoading] = useState(false);

    useEffect(() => {
        const fetch = async () => {
            setLoading(true);
            try {
                const { data } = await axios.get<HelpRequest>(
                    `${backendURL}/help-requests/${id}`
                );
                setRequest(data);
            } catch {
                toast.error("No se pudo cargar la solicitud.");
                navigate(-1);
            } finally {
                setLoading(false);
            }
        };
        if (id) fetch();
    }, [id, backendURL, navigate]);

    const isOwner = request?.requesterId === userData?.id;

    const handleAccept = async () => {
        if (!confirm("¿Aceptar esta solicitud como voluntario?")) return;
        setActionLoading(true);
        try {
            const { data } = await axios.post<HelpRequest>(
                `${backendURL}/help-requests/${id}/accept`
            );
            setRequest(data);
            toast.success("¡Solicitud aceptada! Ya puedes coordinarte con el solicitante.");
        } catch (error: unknown) {
            if (axios.isAxiosError(error)) {
                toast.error(error.response?.data?.message || "Error al aceptar.");
            }
        } finally {
            setActionLoading(false);
        }
    };

    const handleComplete = async () => {
        if (!confirm("¿Marcar esta solicitud como completada?")) return;
        setActionLoading(true);
        try {
            const { data } = await axios.post<HelpRequest>(
                `${backendURL}/help-requests/${id}/complete`
            );
            setRequest(data);
            toast.success("¡Solicitud completada! Gracias por tu ayuda.");
        } catch (error: unknown) {
            if (axios.isAxiosError(error)) {
                toast.error(error.response?.data?.message || "Error al completar.");
            }
        } finally {
            setActionLoading(false);
        }
    };

    const handleCancel = async () => {
        if (!confirm("¿Cancelar esta solicitud?")) return;
        setActionLoading(true);
        try {
            const { data } = await axios.post<HelpRequest>(
                `${backendURL}/help-requests/${id}/cancel`
            );
            setRequest(data);
            toast.success("Solicitud cancelada.");
        } catch (error: unknown) {
            if (axios.isAxiosError(error)) {
                toast.error(error.response?.data?.message || "Error al cancelar.");
            }
        } finally {
            setActionLoading(false);
        }
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
                <div className="spinner-border" style={{ color: "#667eea" }} />
            </div>
        </div>
    );

    if (!request) return null;

    const statusColor = HELP_REQUEST_STATUS_COLORS[request.status];

    return (
        <div className="d-flex flex-column min-vh-100" style={{ background: "#f8f9fa" }}>
            <Menubar />
            <div className="container py-4" style={{ maxWidth: 760 }}>

                {/* Botón volver */}
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
                                        {HELP_REQUEST_STATUS_LABELS[request.status]}
                                    </span>
                                    <span className="text-muted small">
                                        {HELP_REQUEST_TYPE_LABELS[request.type]}
                                    </span>
                                </div>
                                <h3 className="fw-bold mb-0">{request.title}</h3>
                            </div>

                            {/* Acciones del propietario */}
                            {isOwner && request.status === "OPEN" && (
                                <div className="d-flex gap-2">
                                    <button className="btn btn-sm btn-outline-secondary"
                                        style={{ borderRadius: 8 }}
                                        onClick={() => navigate(`/help-requests/${id}/edit`)}>
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
                            {request.description}
                        </p>

                        {/* Metadatos */}
                        <div className="row g-3 mb-4">
                            <div className="col-6 col-md-3">
                                <div className="text-muted small">Creada</div>
                                <div className="fw-medium small">{formatDate(request.createdAt)}</div>
                            </div>
                            {request.deadline && (
                                <div className="col-6 col-md-3">
                                    <div className="text-muted small">Fecha límite</div>
                                    <div className="fw-medium small">{formatDate(request.deadline)}</div>
                                </div>
                            )}
                            {request.acceptedAt && (
                                <div className="col-6 col-md-3">
                                    <div className="text-muted small">Aceptada</div>
                                    <div className="fw-medium small">{formatDate(request.acceptedAt)}</div>
                                </div>
                            )}
                            {request.completedAt && (
                                <div className="col-6 col-md-3">
                                    <div className="text-muted small">Completada</div>
                                    <div className="fw-medium small">{formatDate(request.completedAt)}</div>
                                </div>
                            )}
                        </div>

                        {/* Estimación de viaje (si tiene ubicación y el usuario no es el creador) */}
                        {!isOwner && request.estimatedDistanceMeters && (
                            <div className="alert py-2 px-3 mb-4 d-flex gap-4"
                                style={{
                                    background: "rgba(102,126,234,0.08)",
                                    border: "1px solid rgba(102,126,234,0.2)",
                                    borderRadius: 10
                                }}>
                                <div>
                                    <div className="text-muted small">Distancia</div>
                                    <div className="fw-semibold">
                                        📍 {formatDist(request.estimatedDistanceMeters)}
                                    </div>
                                </div>
                                {request.estimatedTravelSeconds && (
                                    <div>
                                        <div className="text-muted small">Tiempo estimado</div>
                                        <div className="fw-semibold">
                                            ⏱ {formatTime(request.estimatedTravelSeconds)}
                                        </div>
                                    </div>
                                )}
                                {request.fastestTravelSeconds && request.fastestTransportMode && (
                                    <div>
                                        <div className="text-muted small">Más rápido</div>
                                        <div className="fw-semibold">
                                            ⚡ {formatTime(request.fastestTravelSeconds)}
                                        </div>
                                    </div>
                                )}
                            </div>
                        )}

                        {/* Mapa de ubicación */}
                        {request.latitude && request.longitude && (
                            <div className="mb-4">
                                <div className="text-muted small mb-2 fw-medium">📍 Ubicación</div>
                                <MapContainer
                                    center={[request.latitude, request.longitude]}
                                    zoom={15}
                                    style={{ height: 220, borderRadius: 10 }}
                                    scrollWheelZoom={false}
                                >
                                    <TileLayer
                                        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                                        attribution='&copy; OpenStreetMap'
                                    />
                                    <Marker position={[request.latitude, request.longitude]}>
                                        <Popup>{request.title}</Popup>
                                    </Marker>
                                </MapContainer>
                            </div>
                        )}

                        {/* Acciones del voluntario */}
                        <div className="d-flex gap-2 flex-wrap">
                            {!isOwner && request.status === "OPEN" && (
                                <button className="btn px-4"
                                    style={{
                                        background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
                                        color: "white", border: "none", borderRadius: 8, fontWeight: 600
                                    }}
                                    disabled={actionLoading}
                                    onClick={handleAccept}>
                                    {actionLoading && <span className="spinner-border spinner-border-sm me-2" />}
                                    🙋 Aceptar como voluntario
                                </button>
                            )}

                            {isOwner && request.status === "ACCEPTED" && (
                                <button className="btn px-4"
                                    style={{
                                        background: "linear-gradient(135deg, #28a745 0%, #20c997 100%)",
                                        color: "white", border: "none", borderRadius: 8, fontWeight: 600
                                    }}
                                    disabled={actionLoading}
                                    onClick={handleComplete}>
                                    {actionLoading && <span className="spinner-border spinner-border-sm me-2" />}
                                    ✅ Marcar como completada
                                </button>
                            )}

                            {request.status === "COMPLETED" && (
                                <button className="btn btn-sm" style={{
                                    background: "rgba(102,126,234,0.1)",
                                    border: "1px solid rgba(102,126,234,0.3)",
                                    color: "#667eea", borderRadius: 8
                                }} onClick={() => {
                                    const targetId = isOwner ? request.volunteerId : request.requesterId;
                                    navigate(`/reviews/new?targetId=${targetId}&helpRequestId=${request.id}`);
                                }}>
                                    ⭐ Dejar reseña
                                </button>
                            )}


                            {request.cancelReason && (
                                <div className="alert alert-warning py-2 px-3 mb-0 small w-100">
                                    Motivo de cancelación: {request.cancelReason}
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default HelpRequestDetail;