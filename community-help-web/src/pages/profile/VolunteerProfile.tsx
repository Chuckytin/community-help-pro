import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { toast } from "react-toastify";
import Menubar from "../../components/Menubar";
import { useAppContext } from "../../hooks/useAppContext";
import {
    VolunteerProfile, VolunteerSkill,
    SKILL_LABELS, TRANSPORT_LABELS, TransportMode
} from "../../types/volunteer";

/**
 * Página de perfil de voluntario.
 * Permite registrarse como voluntario, actualizar habilidades,
 * modo de transporte, radio de acción y preferencias de notificación.
 *
 * Si el usuario aún no es voluntario, muestra el formulario de registro.
 * Si ya lo es, muestra el formulario de actualización.
 */
const VolunteerProfilePage = () => {
    const navigate = useNavigate();
    const { backendURL } = useAppContext();

    const [profile, setProfile] = useState<VolunteerProfile | null>(null);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [isRegistered, setIsRegistered] = useState(false);

    // Campos del formulario
    const [available, setAvailable] = useState(true);
    const [radiusKm, setRadiusKm] = useState<number>(5);
    const [selectedSkills, setSelectedSkills] = useState<VolunteerSkill[]>([]);
    const [transportMode, setTransportMode] = useState<TransportMode>("FOOT_WALKING");
    const [emailNotifications, setEmailNotifications] = useState(true);

    useEffect(() => {
        const fetch = async () => {
            try {
                const { data } = await axios.get<VolunteerProfile>(
                    `${backendURL}/volunteers/me`
                );
                setProfile(data);
                setIsRegistered(true);
                // Rellena los campos con los valores actuales
                setAvailable(data.available);
                setRadiusKm(data.radiusKm ?? 5);
                setSelectedSkills(data.skills ?? []);
                setTransportMode(data.transportMode ?? "FOOT_WALKING");
                setEmailNotifications(data.emailNotificationsEnabled);
            } catch {
                // 404 = no es voluntario todavía, es normal
                setIsRegistered(false);
            } finally {
                setLoading(false);
            }
        };
        fetch();
    }, [backendURL]);

    const toggleSkill = (skill: VolunteerSkill) => {
        setSelectedSkills(prev =>
            prev.includes(skill)
                ? prev.filter(s => s !== skill)
                : prev.length >= 20 ? prev : [...prev, skill]
        );
    };

    const onSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setSaving(true);
        const payload = {
            available,
            radiusKm,
            skills: selectedSkills,
            transportMode,
            ...(isRegistered ? { emailNotificationsEnabled: emailNotifications } : {}),
        };
        try {
            if (isRegistered) {
                const { data } = await axios.patch<VolunteerProfile>(
                    `${backendURL}/volunteers/me`, payload
                );
                setProfile(data);
                toast.success("Perfil de voluntario actualizado.");
            } else {
                const { data } = await axios.post<VolunteerProfile>(
                    `${backendURL}/volunteers/me`, payload
                );
                setProfile(data);
                setIsRegistered(true);
                toast.success("¡Ahora eres voluntario! Recibirás propuestas automáticamente.");
            }
        } catch (error: unknown) {
            if (axios.isAxiosError(error)) {
                toast.error(error.response?.data?.message || "Error al guardar.");
            }
        } finally {
            setSaving(false);
        }
    };

    const handleDelete = async () => {
        if (!confirm("¿Eliminar tu perfil de voluntario? Dejarás de recibir propuestas.")) return;
        try {
            await axios.delete(`${backendURL}/volunteers/me`);
            setProfile(null);
            setIsRegistered(false);
            toast.success("Perfil de voluntario eliminado.");
        } catch {
            toast.error("No se pudo eliminar el perfil.");
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
            <div className="container py-4" style={{ maxWidth: 680 }}>

                <div className="d-flex align-items-center gap-3 mb-4">
                    <button className="btn btn-sm btn-outline-secondary rounded-pill"
                        onClick={() => navigate(-1)}>
                        ← Volver
                    </button>
                    <h4 className="fw-bold mb-0">🙋 Perfil de voluntario</h4>
                </div>

                {/* Banner si no es voluntario */}
                {!isRegistered && (
                    <div className="alert mb-4" style={{
                        background: "rgba(102,126,234,0.08)",
                        border: "1px solid rgba(102,126,234,0.2)",
                        borderRadius: 10
                    }}>
                        <strong>¿Quieres ayudar a tu comunidad?</strong>
                        <p className="mb-0 mt-1 text-muted small">
                            Al registrarte como voluntario, el sistema te enviará propuestas automáticas
                            cuando haya solicitudes o donaciones cercanas que encajen con tus habilidades.
                        </p>
                    </div>
                )}

                {/* Stats si ya es voluntario */}
                {isRegistered && profile && (
                    <div className="row g-3 mb-4">
                        <div className="col-4">
                            <div className="card shadow-sm text-center py-3" style={{ borderRadius: 10 }}>
                                <div className="fw-bold fs-4" style={{ color: "#667eea" }}>
                                    {profile.rating?.toFixed(1) ?? "—"}
                                </div>
                                <div className="text-muted small">⭐ Rating</div>
                            </div>
                        </div>
                        <div className="col-4">
                            <div className="card shadow-sm text-center py-3" style={{ borderRadius: 10 }}>
                                <div className="fw-bold fs-4" style={{ color: "#28a745" }}>
                                    {profile.radiusKm ?? "—"} km
                                </div>
                                <div className="text-muted small">Radio</div>
                            </div>
                        </div>
                        <div className="col-4">
                            <div className="card shadow-sm text-center py-3" style={{ borderRadius: 10 }}>
                                <div className="fw-bold fs-4" style={{
                                    color: profile.available ? "#28a745" : "#dc3545"
                                }}>
                                    {profile.available ? "✓" : "✗"}
                                </div>
                                <div className="text-muted small">Disponible</div>
                            </div>
                        </div>
                    </div>
                )}

                <div className="card shadow-sm" style={{ borderRadius: 12 }}>
                    <div className="card-body p-4">
                        <form onSubmit={onSubmit}>

                            {/* Disponibilidad */}
                            <div className="mb-4 d-flex align-items-center justify-content-between">
                                <div>
                                    <div className="fw-medium">Disponibilidad</div>
                                    <div className="text-muted small">
                                        Desactiva si no puedes aceptar propuestas temporalmente
                                    </div>
                                </div>
                                <div className="form-check form-switch mb-0">
                                    <input className="form-check-input" type="checkbox"
                                        style={{ width: 48, height: 24, cursor: "pointer" }}
                                        checked={available}
                                        onChange={e => setAvailable(e.target.checked)} />
                                </div>
                            </div>

                            {/* Radio de acción */}
                            <div className="mb-4">
                                <label className="form-label fw-medium">
                                    Radio de acción: <strong>{radiusKm} km</strong>
                                </label>
                                <input type="range" className="form-range"
                                    min={1} max={50} step={1}
                                    value={radiusKm}
                                    onChange={e => setRadiusKm(Number(e.target.value))} />
                                <div className="d-flex justify-content-between text-muted"
                                    style={{ fontSize: "0.75rem" }}>
                                    <span>1 km</span>
                                    <span>50 km</span>
                                </div>
                            </div>

                            {/* Modo de transporte */}
                            <div className="mb-4">
                                <label className="form-label fw-medium">Modo de transporte</label>
                                <div className="d-flex gap-2">
                                    {(Object.keys(TRANSPORT_LABELS) as TransportMode[]).map(mode => (
                                        <button key={mode} type="button"
                                            className="btn grow"
                                            style={{
                                                borderRadius: 8,
                                                border: transportMode === mode
                                                    ? "2px solid #667eea"
                                                    : "1px solid #e2e8f0",
                                                background: transportMode === mode
                                                    ? "rgba(102,126,234,0.1)"
                                                    : "white",
                                                color: transportMode === mode ? "#667eea" : "#4a5568",
                                                fontWeight: transportMode === mode ? 600 : 400,
                                            }}
                                            onClick={() => setTransportMode(mode)}>
                                            {TRANSPORT_LABELS[mode]}
                                        </button>
                                    ))}
                                </div>
                                <div className="form-text">
                                    Se usa para calcular tiempos de viaje reales con OpenRouteService.
                                </div>
                            </div>

                            {/* Habilidades */}
                            <div className="mb-4">
                                <label className="form-label fw-medium">
                                    Habilidades
                                    <span className="text-muted small ms-2">
                                        ({selectedSkills.length}/20 seleccionadas)
                                    </span>
                                </label>
                                <div className="d-flex flex-wrap gap-2">
                                    {(Object.keys(SKILL_LABELS) as VolunteerSkill[]).map(skill => (
                                        <button key={skill} type="button"
                                            className="btn btn-sm"
                                            style={{
                                                borderRadius: 20,
                                                border: selectedSkills.includes(skill)
                                                    ? "2px solid #667eea"
                                                    : "1px solid #e2e8f0",
                                                background: selectedSkills.includes(skill)
                                                    ? "rgba(102,126,234,0.1)"
                                                    : "white",
                                                color: selectedSkills.includes(skill) ? "#667eea" : "#4a5568",
                                                fontSize: "0.78rem",
                                                fontWeight: selectedSkills.includes(skill) ? 600 : 400,
                                            }}
                                            onClick={() => toggleSkill(skill)}>
                                            {SKILL_LABELS[skill]}
                                        </button>
                                    ))}
                                </div>
                            </div>

                            {/* Notificaciones por email */}
                            {isRegistered && (
                                <div className="mb-4 d-flex align-items-center justify-content-between">
                                    <div>
                                        <div className="fw-medium">Notificaciones por email</div>
                                        <div className="text-muted small">
                                            Recibe un digest con las nuevas propuestas cercanas
                                        </div>
                                    </div>
                                    <div className="form-check form-switch mb-0">
                                        <input className="form-check-input" type="checkbox"
                                            style={{ width: 48, height: 24, cursor: "pointer" }}
                                            checked={emailNotifications}
                                            onChange={e => setEmailNotifications(e.target.checked)} />
                                    </div>
                                </div>
                            )}

                            {/* Botones */}
                            <div className="d-flex gap-2 justify-content-between">
                                {isRegistered && (
                                    <button type="button"
                                        className="btn btn-outline-danger"
                                        style={{ borderRadius: 8 }}
                                        onClick={handleDelete}>
                                        Eliminar perfil
                                    </button>
                                )}
                                <button type="submit" disabled={saving}
                                    className="btn px-4 ms-auto"
                                    style={{
                                        background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
                                        color: "white", border: "none", borderRadius: 8, fontWeight: 600
                                    }}>
                                    {saving && <span className="spinner-border spinner-border-sm me-2" />}
                                    {saving ? "Guardando..." : isRegistered ? "Guardar cambios" : "Registrarme como voluntario"}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default VolunteerProfilePage;