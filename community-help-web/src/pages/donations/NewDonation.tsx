import { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { toast } from "react-toastify";
import Menubar from "../../components/Menubar";
import LocationPickerMap from "../../components/map/LocationPickerMap";
import { useAppContext } from "../../hooks/useAppContext";
import { useGeolocation } from "../../hooks/useGeolocation";
import {
    DonationType, FoodType,
    DONATION_TYPE_LABELS, FOOD_TYPE_LABELS
} from "../../types/donation";

/**
 * Formulario para crear una nueva donación.
 * - Si el tipo es FOOD, aparece el selector de subtipo de alimento
 * - Cantidad y unidad son obligatorios
 * - Fecha de caducidad y ubicación son opcionales
 */
const NewDonation = () => {
    const navigate = useNavigate();
    const { backendURL } = useAppContext();
    const { lat: userLat, lon: userLon } = useGeolocation();

    const [donationType, setDonationType] = useState<DonationType>("OTHER");
    const [foodType, setFoodType] = useState<FoodType | "">("");
    const [title, setTitle] = useState("");
    const [description, setDescription] = useState("");
    const [quantity, setQuantity] = useState<number | "">("");
    const [unit, setUnit] = useState("");
    const [expiryDate, setExpiryDate] = useState("");
    const [lat, setLat] = useState<number | null>(null);
    const [lon, setLon] = useState<number | null>(null);
    const [loading, setLoading] = useState(false);

    const mapCenterLat = lat ?? userLat ?? 43.5322;
    const mapCenterLon = lon ?? userLon ?? -5.6611;

    const onSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!quantity || quantity <= 0) {
            toast.error("La cantidad debe ser mayor que 0.");
            return;
        }
        setLoading(true);
        try {
            await axios.post(`${backendURL}/donations`, {
                donationType,
                foodType: donationType === "FOOD" && foodType ? foodType : null,
                title,
                description,
                quantity,
                unit,
                expiryDate: expiryDate ? expiryDate + ":00" : null,
                latitude: lat,
                longitude: lon,
            });
            toast.success("¡Donación publicada! El sistema buscará voluntarios automáticamente.");
            navigate("/donations/me");
        } catch (error: unknown) {
            if (axios.isAxiosError(error)) {
                toast.error(error.response?.data?.message || "Error al crear la donación.");
            } else {
                toast.error("Error al crear la donación.");
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="d-flex flex-column min-vh-100" style={{ background: "#f8f9fa" }}>
            <Menubar />
            <div className="container py-4" style={{ maxWidth: 700 }}>

                <div className="d-flex align-items-center gap-3 mb-4">
                    <button className="btn btn-sm btn-outline-secondary rounded-pill"
                        onClick={() => navigate(-1)}>
                        ← Volver
                    </button>
                    <h4 className="fw-bold mb-0">Nueva donación</h4>
                </div>

                <div className="card shadow-sm" style={{ borderRadius: 12 }}>
                    <div className="card-body p-4">
                        <form onSubmit={onSubmit}>

                            {/* Tipo de donación */}
                            <div className="mb-4">
                                <label className="form-label fw-medium">
                                    Tipo de donación <span className="text-danger">*</span>
                                </label>
                                <div className="row g-2">
                                    {(Object.keys(DONATION_TYPE_LABELS) as DonationType[]).map(t => (
                                        <div key={t} className="col-6 col-md-4">
                                            <button type="button" className="btn w-100 text-start"
                                                style={{
                                                    borderRadius: 8, fontSize: "0.85rem",
                                                    border: donationType === t
                                                        ? "2px solid #28a745"
                                                        : "1px solid #e2e8f0",
                                                    background: donationType === t
                                                        ? "rgba(40,167,69,0.1)"
                                                        : "white",
                                                    color: donationType === t ? "#28a745" : "#4a5568",
                                                    fontWeight: donationType === t ? 600 : 400,
                                                }}
                                                onClick={() => {
                                                    setDonationType(t);
                                                    if (t !== "FOOD") setFoodType("");
                                                }}>
                                                {DONATION_TYPE_LABELS[t]}
                                            </button>
                                        </div>
                                    ))}
                                </div>
                            </div>

                            {/* Subtipo de alimento (solo si FOOD) */}
                            {donationType === "FOOD" && (
                                <div className="mb-3">
                                    <label className="form-label fw-medium">
                                        Tipo de alimento
                                        <span className="text-muted small ms-2">(opcional)</span>
                                    </label>
                                    <div className="d-flex flex-wrap gap-2">
                                        {(Object.keys(FOOD_TYPE_LABELS) as FoodType[]).map(ft => (
                                            <button key={ft} type="button"
                                                className="btn btn-sm"
                                                style={{
                                                    borderRadius: 20,
                                                    border: foodType === ft
                                                        ? "2px solid #28a745"
                                                        : "1px solid #e2e8f0",
                                                    background: foodType === ft
                                                        ? "rgba(40,167,69,0.1)"
                                                        : "white",
                                                    color: foodType === ft ? "#28a745" : "#4a5568",
                                                }}
                                                onClick={() => setFoodType(foodType === ft ? "" : ft)}>
                                                {FOOD_TYPE_LABELS[ft]}
                                            </button>
                                        ))}
                                    </div>
                                </div>
                            )}

                            {/* Título */}
                            <div className="mb-3">
                                <label className="form-label fw-medium">
                                    Título <span className="text-danger">*</span>
                                </label>
                                <input type="text" className="form-control"
                                    style={{ borderRadius: 8, border: "1px solid #e2e8f0" }}
                                    placeholder="Ej: Ropa de invierno para adulto"
                                    maxLength={120} required
                                    value={title} onChange={e => setTitle(e.target.value)} />
                                <div className="text-end text-muted small mt-1">{title.length}/120</div>
                            </div>

                            {/* Descripción */}
                            <div className="mb-3">
                                <label className="form-label fw-medium">
                                    Descripción <span className="text-danger">*</span>
                                </label>
                                <textarea className="form-control"
                                    style={{ borderRadius: 8, border: "1px solid #e2e8f0" }}
                                    placeholder="Describe el estado, talla, condiciones..."
                                    rows={3} maxLength={1000} required
                                    value={description}
                                    onChange={e => setDescription(e.target.value)} />
                                <div className="text-end text-muted small mt-1">
                                    {description.length}/1000
                                </div>
                            </div>

                            {/* Cantidad y unidad */}
                            <div className="row g-3 mb-3">
                                <div className="col-5">
                                    <label className="form-label fw-medium">
                                        Cantidad <span className="text-danger">*</span>
                                    </label>
                                    <input type="number" className="form-control"
                                        style={{ borderRadius: 8, border: "1px solid #e2e8f0" }}
                                        placeholder="1" min={1} required
                                        value={quantity}
                                        onChange={e => setQuantity(e.target.value ? Number(e.target.value) : "")} />
                                </div>
                                <div className="col-7">
                                    <label className="form-label fw-medium">
                                        Unidad <span className="text-danger">*</span>
                                    </label>
                                    <input type="text" className="form-control"
                                        style={{ borderRadius: 8, border: "1px solid #e2e8f0" }}
                                        placeholder="kg, bolsas, prendas, cajas..."
                                        maxLength={30} required
                                        value={unit}
                                        onChange={e => setUnit(e.target.value)} />
                                </div>
                            </div>

                            {/* Fecha de caducidad (opcional) */}
                            <div className="mb-4">
                                <label className="form-label fw-medium">
                                    Fecha de caducidad / disponibilidad
                                    <span className="text-muted small ms-2">(opcional)</span>
                                </label>
                                <input type="datetime-local" className="form-control"
                                    style={{ borderRadius: 8, border: "1px solid #e2e8f0" }}
                                    min={new Date().toISOString().slice(0, 16)}
                                    value={expiryDate}
                                    onChange={e => setExpiryDate(e.target.value)} />
                                <div className="form-text">
                                    Pasada esta fecha, la donación se marcará como expirada automáticamente.
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
                                    onChange={(lt, ln) => { setLat(lt); setLon(ln); }}
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
                                        Sin ubicación — los voluntarios no verán distancia.
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
                                        background: "linear-gradient(135deg, #28a745 0%, #20c997 100%)",
                                        color: "white", border: "none", borderRadius: 8, fontWeight: 600
                                    }}>
                                    {loading && <span className="spinner-border spinner-border-sm me-2" />}
                                    {loading ? "Publicando..." : "Publicar donación"}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default NewDonation;