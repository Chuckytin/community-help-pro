import { useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import axios from "axios";
import { toast } from "react-toastify";
import Menubar from "../../components/Menubar";
import { useAppContext } from "../../hooks/useAppContext";

/**
 * Formulario para dejar una reseña.
 * Se accede desde la página de detalle tras completar una solicitud o donación.
 *
 * Query params esperados:
 * - targetId: UUID del usuario a valorar
 * - targetName: nombre del usuario a valorar
 * - donationId: UUID de la donación (si aplica)
 * - helpRequestId: UUID de la solicitud (si aplica)
 */
const ReviewForm = () => {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const { backendURL } = useAppContext();

    const targetId = searchParams.get("targetId") ?? "";
    const targetName = searchParams.get("targetName") ?? "este usuario";
    const donationId = searchParams.get("donationId");
    const helpRequestId = searchParams.get("helpRequestId");

    const [rating, setRating] = useState<number>(5);
    const [comment, setComment] = useState("");
    const [loading, setLoading] = useState(false);

    const onSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        try {
            await axios.post(`${backendURL}/reviews`, {
                targetId,
                ...(donationId ? { donationId } : {}),
                ...(helpRequestId ? { helpRequestId } : {}),
                rating,
                comment: comment || null,
            });
            toast.success("¡Reseña enviada! Gracias por tu valoración.");
            navigate(-1);
        } catch (error: unknown) {
            if (axios.isAxiosError(error)) {
                toast.error(error.response?.data?.message || "Error al enviar la reseña.");
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="d-flex flex-column min-vh-100" style={{ background: "#f8f9fa" }}>
            <Menubar />
            <div className="container py-4" style={{ maxWidth: 500 }}>

                <div className="d-flex align-items-center gap-3 mb-4">
                    <button className="btn btn-sm btn-outline-secondary rounded-pill"
                        onClick={() => navigate(-1)}>
                        ← Volver
                    </button>
                    <h4 className="fw-bold mb-0">⭐ Dejar reseña</h4>
                </div>

                <div className="card shadow-sm" style={{ borderRadius: 12 }}>
                    <div className="card-body p-4">

                        <p className="text-muted mb-4">
                            Valora tu experiencia con <strong>{targetName}</strong>
                        </p>

                        <form onSubmit={onSubmit}>

                            {/* Selector de estrellas */}
                            <div className="mb-4">
                                <label className="form-label fw-medium">Puntuación</label>
                                <div className="d-flex gap-2">
                                    {[1, 2, 3, 4, 5].map(star => (
                                        <button key={star} type="button"
                                            style={{
                                                fontSize: "2rem", background: "none",
                                                border: "none", cursor: "pointer",
                                                opacity: star <= rating ? 1 : 0.3,
                                                transition: "opacity 0.15s, transform 0.15s",
                                                transform: star <= rating ? "scale(1.1)" : "scale(1)"
                                            }}
                                            onClick={() => setRating(star)}
                                            onMouseEnter={e => (e.currentTarget.style.transform = "scale(1.2)")}
                                            onMouseLeave={e => (e.currentTarget.style.transform = star <= rating ? "scale(1.1)" : "scale(1)")}>
                                            ⭐
                                        </button>
                                    ))}
                                </div>
                                <div className="text-muted small mt-1">
                                    {["", "Muy malo", "Malo", "Regular", "Bueno", "Excelente"][rating]}
                                </div>
                            </div>

                            {/* Comentario */}
                            <div className="mb-4">
                                <label className="form-label fw-medium">
                                    Comentario
                                    <span className="text-muted small ms-2">(opcional)</span>
                                </label>
                                <textarea className="form-control"
                                    style={{ borderRadius: 8, border: "1px solid #e2e8f0" }}
                                    placeholder="Cuéntanos cómo fue la experiencia..."
                                    rows={4} maxLength={2000}
                                    value={comment}
                                    onChange={e => setComment(e.target.value)} />
                                <div className="text-end text-muted small mt-1">
                                    {comment.length}/2000
                                </div>
                            </div>

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
                                        color: "white", border: "none", borderRadius: 8, fontWeight: 600
                                    }}>
                                    {loading && <span className="spinner-border spinner-border-sm me-2" />}
                                    {loading ? "Enviando..." : "Enviar reseña"}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ReviewForm;