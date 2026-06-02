import { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { toast } from "react-toastify";
import Menubar from "../../components/Menubar";
import LocationPickerMap from "../../components/map/LocationPickerMap";
import { useAppContext } from "../../hooks/useAppContext";
import { useGeolocation } from "../../hooks/useGeolocation";
import { HelpRequestType, HELP_REQUEST_TYPE_LABELS } from "../../types/helpRequest";

/**
 * Formulario para crear una nueva solicitud de ayuda.
 * - Campos: tipo, título, descripción, fecha límite (opcional), ubicación (opcional)
 * - La ubicación se selecciona en el mapa haciendo clic
 * - Al crear, el backend lanza automáticamente el motor de matching
 */
const NewHelpRequest = () => {
    const navigate = useNavigate();
    const { backendURL } = useAppContext();
    const { lat: userLat, lon: userLon } = useGeolocation();

    const [type, setType] = useState<HelpRequestType>("OTHER");
    const [title, setTitle] = useState("");
    const [description, setDescription] = useState("");
    const [deadline, setDeadline] = useState("");
    const [lat, setLat] = useState<number | null>(null);
    const [lon, setLon] = useState<number | null>(null);
    const [loading, setLoading] = useState(false);

    // Cuando la geolocalización llega, inicializa el mapa en la posición del usuario
    // pero no selecciona ubicación aún (lat/lon siguen siendo null)
    const mapCenterLat = lat ?? userLat ?? 43.5322;
    const mapCenterLon = lon ?? userLon ?? -5.6611;

    const handleMapClick = (clickLat: number, clickLon: number) => {
        setLat(clickLat);
        setLon(clickLon);
    };

    const onSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        try {
            await axios.post(`${backendURL}/help-requests`, {
                type,
                title,
                description,
                deadline: deadline ? deadline + ":00" : null, // LocalDateTime necesita segundos
                latitude: lat,
                longitude: lon,
            });
            toast.success("¡Solicitud creada! El sistema buscará voluntarios automáticamente.");
            navigate("/help-requests/me");
        } catch (error: unknown) {
            if (axios.isAxiosError(error)) {
                toast.error(error.response?.data?.message || "Error al crear la solicitud.");
            } else {
                toast.error("Error al crear la solicitud.");
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="d-flex flex-column min-vh-100" style={{ background: "#f8f9fa" }}>
            <Menubar />
            <div className="container py-4" style={{ maxWidth: 700 }}>

                {/* Cabecera */}
                <div className="d-flex align-items-center gap-3 mb-4">
                    <button className="btn btn-sm btn-outline-secondary rounded-pill"
                        onClick={() => navigate(-1)}>
                        ← Volver
                    </button>
                    <h4 className="fw-bold mb-0">Nueva solicitud de ayuda</h4>
                </div>

                <div className="card shadow-sm" style={{ borderRadius: 12 }}>
                    <div className="card-body p-4">
                        <form onSubmit={onSubmit}>

                            {/* Tipo */}
                            <div className="mb-4">
                                <label className="form-label fw-medium">
                                    Tipo de ayuda <span className="text-danger">*</span>
                                </label>
                                <div className="row g-2">
                                    {(Object.keys(HELP_REQUEST_TYPE_LABELS) as HelpRequestType[]).map(t => (
                                        <div key={t} className="col-6 col-md-4">
                                            <button
                                                type="button"
                                                className="btn w-100 text-start"
                                                style={{
                                                    borderRadius: 8, fontSize: "0.85rem",
                                                    border: type === t
                                                        ? "2px solid #667eea"
                                                        : "1px solid #e2e8f0",
                                                    background: type === t
                                                        ? "rgba(102,126,234,0.1)"
                                                        : "white",
                                                    color: type === t ? "#667eea" : "#4a5568",
                                                    fontWeight: type === t ? 600 : 400,
                                                }}
                                                onClick={() => setType(t)}
                                            >
                                                {HELP_REQUEST_TYPE_LABELS[t]}
                                            </button>
                                        </div>
                                    ))}
                                </div>
                            </div>

                            {/* Título */}
                            <div className="mb-3">
                                <label className="form-label fw-medium">
                                    Título <span className="text-danger">*</span>
                                </label>
                                <input type="text" className="form-control"
                                    style={{ borderRadius: 8, border: "1px solid #e2e8f0" }}
                                    placeholder="Ej: Necesito ayuda para hacer la compra"
                                    maxLength={120} required
                                    value={title} onChange={e => setTitle(e.target.value)} />
                                <div className="text-end text-muted small mt-1">
                                    {title.length}/120
                                </div>
                            </div>

                            {/* Descripción */}
                            <div className="mb-3">
                                <label className="form-label fw-medium">
                                    Descripción <span className="text-danger">*</span>
                                </label>
                                <textarea className="form-control"
                                    style={{ borderRadius: 8, border: "1px solid #e2e8f0" }}
                                    placeholder="Explica con detalle qué necesitas..."
                                    rows={4} maxLength={1000} required
                                    value={description}
                                    onChange={e => setDescription(e.target.value)} />
                                <div className="text-end text-muted small mt-1">
                                    {description.length}/1000
                                </div>
                            </div>

                            {/* Fecha límite (opcional) */}
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
                                <div className="form-text">
                                    El motor de matching descartará voluntarios que no puedan llegar a tiempo.
                                </div>
                            </div>

                            {/* Ubicación */}
                            <div className="mb-4">
                                <label className="form-label fw-medium">
                                    Ubicación
                                    <span className="text-muted small ms-2">(opcional — haz clic en el mapa)</span>
                                </label>
                                <LocationPickerMap
                                    lat={mapCenterLat}
                                    lon={mapCenterLon}
                                    onChange={handleMapClick}
                                    height="280px"
                                />
                                {lat && lon ? (
                                    <div className="d-flex align-items-center justify-content-between mt-2">
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
                                    <p className="text-muted small mt-2">
                                        Sin ubicación seleccionada — los voluntarios no verán distancia.
                                    </p>
                                )}
                            </div>

                            {/* Botones */}
                            <div className="d-flex gap-2 justify-content-end">
                                <button type="button"
                                    className="btn btn-outline-secondary"
                                    style={{ borderRadius: 8 }}
                                    onClick={() => navigate(-1)}>
                                    Cancelar
                                </button>
                                <button type="submit" disabled={loading}
                                    className="btn px-4"
                                    style={{
                                        background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
                                        color: "white", border: "none", borderRadius: 8,
                                        fontWeight: 600
                                    }}>
                                    {loading && <span className="spinner-border spinner-border-sm me-2" />}
                                    {loading ? "Creando..." : "Crear solicitud"}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default NewHelpRequest;