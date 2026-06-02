import { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { toast } from "react-toastify";
import Menubar from "../../components/Menubar";
import LocationPickerMap from "../../components/map/LocationPickerMap";
import { useAppContext } from "../../hooks/useAppContext";

/**
 * Página de perfil del usuario.
 * Permite actualizar nombre, email, contraseña y ubicación.
 * La ubicación se usa para el motor de matching y las búsquedas de proximidad.
 */
const UserProfile = () => {
    const navigate = useNavigate();
    const { backendURL, userData, getUserData } = useAppContext();

    const [name, setName] = useState(userData?.name ?? "");
    const [email, setEmail] = useState(userData?.email ?? "");
    const [password, setPassword] = useState("");
    const [lat, setLat] = useState<number | null>(userData?.latitude ?? null);
    const [lon, setLon] = useState<number | null>(userData?.longitude ?? null);
    const [loading, setLoading] = useState(false);

    const mapCenterLat = lat ?? 43.5322;
    const mapCenterLon = lon ?? -5.6611;

    const onSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        try {
            await axios.patch(`${backendURL}/users/me`, {
                ...(name !== userData?.name ? { name } : {}),
                ...(email !== userData?.email ? { email } : {}),
                ...(password ? { password } : {}),
                ...(lat !== null ? { latitude: lat } : {}),
                ...(lon !== null ? { longitude: lon } : {}),
            });
            await getUserData();
            toast.success("Perfil actualizado correctamente.");
            setPassword("");
        } catch (error: unknown) {
            if (axios.isAxiosError(error)) {
                toast.error(error.response?.data?.message || "Error al actualizar el perfil.");
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="d-flex flex-column min-vh-100" style={{ background: "#f8f9fa" }}>
            <Menubar />
            <div className="container py-4" style={{ maxWidth: 600 }}>

                <div className="d-flex align-items-center gap-3 mb-4">
                    <button className="btn btn-sm btn-outline-secondary rounded-pill"
                        onClick={() => navigate(-1)}>
                        ← Volver
                    </button>
                    <h4 className="fw-bold mb-0">👤 Mi perfil</h4>
                </div>

                {/* Avatar */}
                <div className="text-center mb-4">
                    <div className="rounded-circle d-flex align-items-center justify-content-center mx-auto"
                        style={{
                            width: 80, height: 80,
                            background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
                            fontSize: "2rem", color: "white", fontWeight: 700
                        }}>
                        {userData?.name[0].toUpperCase()}
                    </div>
                    {userData?.rating !== undefined && userData.rating !== null && (
                        <div className="text-muted small mt-2">
                            ⭐ Rating: {userData.rating.toFixed(1)}
                        </div>
                    )}
                    <div className="mt-1">
                        <span className="badge" style={{
                            background: userData?.emailVerified
                                ? "rgba(40,167,69,0.1)" : "rgba(255,193,7,0.2)",
                            color: userData?.emailVerified ? "#28a745" : "#856404",
                            borderRadius: 20, fontSize: "0.75rem"
                        }}>
                            {userData?.emailVerified ? "✓ Email verificado" : "⚠️ Email no verificado"}
                        </span>
                    </div>
                </div>

                <div className="card shadow-sm" style={{ borderRadius: 12 }}>
                    <div className="card-body p-4">
                        <form onSubmit={onSubmit}>

                            <div className="mb-3">
                                <label className="form-label fw-medium">Nombre</label>
                                <input type="text" className="form-control"
                                    style={{ borderRadius: 8, border: "1px solid #e2e8f0" }}
                                    value={name} onChange={e => setName(e.target.value)}
                                    minLength={2} maxLength={30} />
                            </div>

                            <div className="mb-3">
                                <label className="form-label fw-medium">Email</label>
                                <input type="email" className="form-control"
                                    style={{ borderRadius: 8, border: "1px solid #e2e8f0" }}
                                    value={email} onChange={e => setEmail(e.target.value)} />
                            </div>

                            <div className="mb-4">
                                <label className="form-label fw-medium">
                                    Nueva contraseña
                                    <span className="text-muted small ms-2">(déjalo vacío para no cambiarla)</span>
                                </label>
                                <input type="password" className="form-control"
                                    style={{ borderRadius: 8, border: "1px solid #e2e8f0" }}
                                    placeholder="••••••••" minLength={6}
                                    value={password} onChange={e => setPassword(e.target.value)} />
                            </div>

                            {/* Ubicación */}
                            <div className="mb-4">
                                <label className="form-label fw-medium">
                                    Mi ubicación
                                    <span className="text-muted small ms-2">
                                        (haz clic en el mapa — se usa para buscar solicitudes cercanas)
                                    </span>
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
                                    <p className="text-muted small mt-2">
                                        Sin ubicación — no aparecerás en búsquedas de proximidad.
                                    </p>
                                )}
                            </div>

                            <div className="d-flex gap-2 justify-content-between">
                                <button type="button"
                                    className="btn btn-outline-danger"
                                    style={{ borderRadius: 8 }}
                                    onClick={() => navigate("/profile/volunteer")}>
                                    🙋 Perfil de voluntario →
                                </button>
                                <button type="submit" disabled={loading}
                                    className="btn px-4"
                                    style={{
                                        background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
                                        color: "white", border: "none", borderRadius: 8, fontWeight: 600
                                    }}>
                                    {loading && <span className="spinner-border spinner-border-sm me-2" />}
                                    {loading ? "Guardando..." : "Guardar cambios"}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default UserProfile;