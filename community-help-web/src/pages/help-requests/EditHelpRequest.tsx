import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import axios from "axios";
import { toast } from "react-toastify";
import Menubar from "../../components/Menubar";
import LocationPickerMap from "../../components/map/LocationPickerMap";
import { useAppContext } from "../../hooks/useAppContext";
import { HelpRequest, HelpRequestType, HELP_REQUEST_TYPE_LABELS } from "../../types/helpRequest";

/**
 * Formulario de edición de una solicitud de ayuda.
 * Solo disponible para solicitudes OPEN del usuario autenticado.
 * Si cambia la ubicación o el tipo, el backend regenera las proposals automáticamente.
 */
const EditHelpRequest = () => {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const { backendURL } = useAppContext();

    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);

    const [type, setType] = useState<HelpRequestType>("OTHER");
    const [title, setTitle] = useState("");
    const [description, setDescription] = useState("");
    const [deadline, setDeadline] = useState("");
    const [lat, setLat] = useState<number | null>(null);
    const [lon, setLon] = useState<number | null>(null);

    /** Carga los datos actuales de la solicitud para pre-rellenar el formulario */
    useEffect(() => {
        const fetch = async () => {
            try {
                const { data } = await axios.get<HelpRequest>(
                    `${backendURL}/help-requests/me/${id}`
                );
                setType(data.type);
                setTitle(data.title);
                setDescription(data.description);
                setDeadline(data.deadline
                    ? data.deadline.slice(0, 16) // "2024-12-31T23:59"
                    : "");
                setLat(data.latitude);
                setLon(data.longitude);
            } catch {
                toast.error("No se pudo cargar la solicitud.");
                navigate(-1);
            } finally {
                setLoading(false);
            }
        };
        if (id) fetch();
    // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [id, backendURL]);

    const mapCenterLat = lat ?? 43.5322;
    const mapCenterLon = lon ?? -5.6611;

    const onSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setSaving(true);
        try {
            await axios.patch(`${backendURL}/help-requests/${id}`, {
                type,
                title,
                description,
                deadline: deadline ? deadline + ":00" : null,
                latitude: lat,
                longitude: lon,
            });
            toast.success("Solicitud actualizada correctamente.");
            navigate(`/help-requests/${id}`);
        } catch (error: unknown) {
            if (axios.isAxiosError(error)) {
                toast.error(error.response?.data?.message || "Error al actualizar.");
            }
        } finally {
            setSaving(false);
        }
    };

    if (loading) return (
        <div className="d-flex flex-column min-vh-100" style={{ background: "#f8f9fa" }}>
            <Menubar />
            <div className="d-flex justify-content-center align-items-center grow">
                <div className="spinner-border" style={{ color: "#667eea" }} />
            </div>
        </div>
    );

    return (
        <div className="d-flex flex-column min-vh-100" style={{ background: "#f8f9fa" }}>
            <Menubar />
            <div className="container py-4" style={{ maxWidth: 700 }}>

                <div className="d-flex align-items-center gap-3 mb-4">
                    <button className="btn btn-sm btn-outline-secondary rounded-pill"
                        onClick={() => navigate(-1)}>
                        ← Volver
                    </button>
                    <h4 className="fw-bold mb-0">✏️ Editar solicitud</h4>
                </div>

                {/* Aviso sobre proposals */}
                <div className="alert py-2 px-3 mb-4 small"
                    style={{
                        background: "rgba(102,126,234,0.08)",
                        border: "1px solid rgba(102,126,234,0.2)",
                        borderRadius: 10
                    }}>
                    ℹ️ Si cambias el tipo o la ubicación, el sistema regenerará las propuestas automáticamente.
                </div>

                <div className="card shadow-sm" style={{ borderRadius: 12 }}>
                    <div className="card-body p-4">
                        <form onSubmit={onSubmit}>

                            {/* Tipo */}
                            <div className="mb-4">
                                <label className="form-label fw-medium">Tipo de ayuda</label>
                                <div className="row g-2">
                                    {(Object.keys(HELP_REQUEST_TYPE_LABELS) as HelpRequestType[]).map(t => (
                                        <div key={t} className="col-6 col-md-4">
                                            <button type="button" className="btn w-100 text-start"
                                                style={{
                                                    borderRadius: 8, fontSize: "0.85rem",
                                                    border: type === t ? "2px solid #667eea" : "1px solid #e2e8f0",
                                                    background: type === t ? "rgba(102,126,234,0.1)" : "white",
                                                    color: type === t ? "#667eea" : "#4a5568",
                                                    fontWeight: type === t ? 600 : 400,
                                                }}
                                                onClick={() => setType(t)}>
                                                {HELP_REQUEST_TYPE_LABELS[t]}
                                            </button>
                                        </div>
                                    ))}
                                </div>
                            </div>

                            {/* Título */}
                            <div className="mb-3">
                                <label className="form-label fw-medium">Título</label>
                                <input type="text" className="form-control"
                                    style={{ borderRadius: 8, border: "1px solid #e2e8f0" }}
                                    maxLength={120} required
                                    value={title} onChange={e => setTitle(e.target.value)} />
                                <div className="text-end text-muted small mt-1">{title.length}/120</div>
                            </div>

                            {/* Descripción */}
                            <div className="mb-3">
                                <label className="form-label fw-medium">Descripción</label>
                                <textarea className="form-control"
                                    style={{ borderRadius: 8, border: "1px solid #e2e8f0" }}
                                    rows={4} maxLength={1000} required
                                    value={description}
                                    onChange={e => setDescription(e.target.value)} />
                                <div className="text-end text-muted small mt-1">
                                    {description.length}/1000
                                </div>
                            </div>

                            {/* Fecha límite */}
                            <div className="mb-4">
                                <label className="form-label fw-medium">
                                    Fecha límite
                                    <span className="text-muted small ms-2">(opcional)</span>
                                </label>
                                <input type="datetime-local" className="form-control"
                                    style={{ borderRadius: 8, border: "1px solid #e2e8f0" }}
                                    min={new Date().toISOString().slice(0, 16)}
                                    value={deadline}
                                    onChange={e => setDeadline(e.target.value)} />
                            </div>

                            {/* Ubicación */}
                            <div className="mb-4">
                                <label className="form-label fw-medium">
                                    Ubicación
                                    <span className="text-muted small ms-2">(haz clic para cambiar)</span>
                                </label>
                                <LocationPickerMap
                                    lat={mapCenterLat}
                                    lon={mapCenterLon}
                                    onChange={(lt, ln) => { setLat(lt); setLon(ln); }}
                                    height="260px"
                                />
                                {lat && lon ? (
                                    <div className="d-flex justify-content-between align-items-center mt-2">
                                        <span className="text-muted small">
                                            📍 {lat.toFixed(5)}, {lon.toFixed(5)}
                                        </span>
                                        <button type="button"
                                            className="btn btn-sm btn-outline-danger"
                                            style={{ borderRadius: 8, fontSize: "0.75rem" }}
                                            onClick={() => { setLat(null); setLon(null); }}>
                                            Quitar ubicación
                                        </button>
                                    </div>
                                ) : (
                                    <p className="text-muted small mt-2">Sin ubicación seleccionada.</p>
                                )}
                            </div>

                            <div className="d-flex gap-2 justify-content-end">
                                <button type="button" className="btn btn-outline-secondary"
                                    style={{ borderRadius: 8 }}
                                    onClick={() => navigate(-1)}>
                                    Cancelar
                                </button>
                                <button type="submit" disabled={saving} className="btn px-4"
                                    style={{
                                        background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
                                        color: "white", border: "none", borderRadius: 8, fontWeight: 600
                                    }}>
                                    {saving && <span className="spinner-border spinner-border-sm me-2" />}
                                    {saving ? "Guardando..." : "Guardar cambios"}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default EditHelpRequest;