import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import axios from "axios";
import { toast } from "react-toastify";
import Menubar from "../../components/Menubar";
import LocationPickerMap from "../../components/map/LocationPickerMap";
import { useAppContext } from "../../hooks/useAppContext";
import {
    Donation, DonationType, FoodType,
    DONATION_TYPE_LABELS, FOOD_TYPE_LABELS
} from "../../types/donation";

/**
 * Formulario de edición de una donación.
 * Solo disponible para donaciones AVAILABLE del usuario autenticado.
 */
const EditDonation = () => {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const { backendURL } = useAppContext();

    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);

    const [donationType, setDonationType] = useState<DonationType>("OTHER");
    const [foodType, setFoodType] = useState<FoodType | "">("");
    const [title, setTitle] = useState("");
    const [description, setDescription] = useState("");
    const [quantity, setQuantity] = useState<number | "">("");
    const [unit, setUnit] = useState("");
    const [expiryDate, setExpiryDate] = useState("");
    const [lat, setLat] = useState<number | null>(null);
    const [lon, setLon] = useState<number | null>(null);

    useEffect(() => {
        const fetch = async () => {
            try {
                const { data } = await axios.get<Donation>(
                    `${backendURL}/donations/${id}`
                );
                setDonationType(data.donationType);
                setFoodType(data.foodType ?? "");
                setTitle(data.title);
                setDescription(data.description);
                setQuantity(data.quantity ?? "");
                setUnit(data.unit ?? "");
                setExpiryDate(data.expiryDate ? data.expiryDate.slice(0, 16) : "");
                setLat(data.latitude);
                setLon(data.longitude);
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

    const mapCenterLat = lat ?? 43.5322;
    const mapCenterLon = lon ?? -5.6611;

    const onSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setSaving(true);
        try {
            await axios.patch(`${backendURL}/donations/${id}`, {
                donationType,
                foodType: donationType === "FOOD" && foodType ? foodType : null,
                title,
                description,
                quantity: quantity || null,
                unit,
                expiryDate: expiryDate ? expiryDate + ":00" : null,
                latitude: lat,
                longitude: lon,
            });
            toast.success("Donación actualizada correctamente.");
            navigate(`/donations/${id}`);
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
                <div className="spinner-border" style={{ color: "#28a745" }} />
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
                    <h4 className="fw-bold mb-0">✏️ Editar donación</h4>
                </div>

                <div className="card shadow-sm" style={{ borderRadius: 12 }}>
                    <div className="card-body p-4">
                        <form onSubmit={onSubmit}>

                            {/* Tipo */}
                            <div className="mb-4">
                                <label className="form-label fw-medium">Tipo de donación</label>
                                <div className="row g-2">
                                    {(Object.keys(DONATION_TYPE_LABELS) as DonationType[]).map(t => (
                                        <div key={t} className="col-6 col-md-4">
                                            <button type="button" className="btn w-100 text-start"
                                                style={{
                                                    borderRadius: 8, fontSize: "0.85rem",
                                                    border: donationType === t ? "2px solid #28a745" : "1px solid #e2e8f0",
                                                    background: donationType === t ? "rgba(40,167,69,0.1)" : "white",
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

                            {/* Subtipo alimento */}
                            {donationType === "FOOD" && (
                                <div className="mb-3">
                                    <label className="form-label fw-medium">
                                        Tipo de alimento
                                        <span className="text-muted small ms-2">(opcional)</span>
                                    </label>
                                    <div className="d-flex flex-wrap gap-2">
                                        {(Object.keys(FOOD_TYPE_LABELS) as FoodType[]).map(ft => (
                                            <button key={ft} type="button" className="btn btn-sm"
                                                style={{
                                                    borderRadius: 20,
                                                    border: foodType === ft ? "2px solid #28a745" : "1px solid #e2e8f0",
                                                    background: foodType === ft ? "rgba(40,167,69,0.1)" : "white",
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
                                    <label className="form-label fw-medium">Cantidad</label>
                                    <input type="number" className="form-control"
                                        style={{ borderRadius: 8, border: "1px solid #e2e8f0" }}
                                        min={1} required
                                        value={quantity}
                                        onChange={e => setQuantity(e.target.value ? Number(e.target.value) : "")} />
                                </div>
                                <div className="col-7">
                                    <label className="form-label fw-medium">Unidad</label>
                                    <input type="text" className="form-control"
                                        style={{ borderRadius: 8, border: "1px solid #e2e8f0" }}
                                        maxLength={30} required
                                        value={unit}
                                        onChange={e => setUnit(e.target.value)} />
                                </div>
                            </div>

                            {/* Fecha caducidad */}
                            <div className="mb-4">
                                <label className="form-label fw-medium">
                                    Fecha de caducidad
                                    <span className="text-muted small ms-2">(opcional)</span>
                                </label>
                                <input type="datetime-local" className="form-control"
                                    style={{ borderRadius: 8, border: "1px solid #e2e8f0" }}
                                    min={new Date().toISOString().slice(0, 16)}
                                    value={expiryDate}
                                    onChange={e => setExpiryDate(e.target.value)} />
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
                                            Quitar
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
                                        background: "linear-gradient(135deg, #28a745 0%, #20c997 100%)",
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

export default EditDonation;