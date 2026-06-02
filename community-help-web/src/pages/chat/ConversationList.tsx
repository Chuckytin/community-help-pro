import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { toast } from "react-toastify";
import Menubar from "../../components/Menubar";
import { useAppContext } from "../../hooks/useAppContext";
import { Conversation } from "../../types/chat";

/**
 * Lista de todas las conversaciones del usuario.
 * Cada conversación se abre al hacer clic.
 * El badge de unreadCount indica mensajes no leídos.
 */
const ConversationList = () => {
    const navigate = useNavigate();
    const { backendURL } = useAppContext();
    const [conversations, setConversations] = useState<Conversation[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetch = async () => {
            try {
                const { data } = await axios.get<{ content: Conversation[] }>(
                    `${backendURL}/conversations`,
                    { params: { size: 50 } }
                );
                setConversations(data.content);
            } catch {
                toast.error("Error al cargar las conversaciones.");
            } finally {
                setLoading(false);
            }
        };
        fetch();
    }, [backendURL]);

    const formatDate = (s: string) =>
        new Date(s).toLocaleDateString("es-ES", {
            day: "2-digit", month: "short",
            hour: "2-digit", minute: "2-digit"
        });

    return (
        <div className="d-flex flex-column min-vh-100" style={{ background: "#f8f9fa" }}>
            <Menubar />
            <div className="container py-4" style={{ maxWidth: 600 }}>

                <h4 className="fw-bold mb-4">💬 Mis conversaciones</h4>

                {loading ? (
                    <div className="text-center py-5">
                        <div className="spinner-border" style={{ color: "#667eea" }} />
                    </div>
                ) : conversations.length === 0 ? (
                    <div className="text-center py-5 text-muted">
                        <span style={{ fontSize: "3rem" }}>💬</span>
                        <p className="mt-3">No tienes conversaciones todavía.</p>
                        <p className="small">
                            Las conversaciones se abren automáticamente cuando
                            un voluntario acepta una solicitud o confirma una donación.
                        </p>
                    </div>
                ) : (
                    <div className="d-flex flex-column gap-2">
                        {conversations.map(c => {
                            const otherParticipants = c.participants.filter(
                                p => p.userId !== c.participants[0]?.userId
                            );
                            const displayName = otherParticipants.length > 0
                                ? otherParticipants.map(p => p.userName).join(", ")
                                : "Conversación";

                            return (
                                <div key={c.id}
                                    className="card shadow-sm"
                                    style={{
                                        borderRadius: 12, cursor: "pointer",
                                        borderLeft: c.unreadCount > 0
                                            ? "4px solid #667eea"
                                            : "4px solid transparent",
                                        transition: "transform 0.15s"
                                    }}
                                    onClick={() => navigate(`/chat/${c.id}`)}
                                    onMouseEnter={e => (e.currentTarget.style.transform = "translateX(4px)")}
                                    onMouseLeave={e => (e.currentTarget.style.transform = "translateX(0)")}>
                                    <div className="card-body d-flex justify-content-between align-items-center py-3">
                                        <div>
                                            <div className="d-flex align-items-center gap-2 mb-1">
                                                <span style={{ fontSize: "1.1rem" }}>
                                                    {c.type === "HELP_REQUEST" ? "🙋" : "🎁"}
                                                </span>
                                                <span className="fw-semibold">{displayName}</span>
                                                {c.unreadCount > 0 && (
                                                    <span className="badge rounded-pill"
                                                        style={{
                                                            background: "#667eea",
                                                            color: "white", fontSize: "0.7rem"
                                                        }}>
                                                        {c.unreadCount}
                                                    </span>
                                                )}
                                            </div>
                                            <div className="text-muted small">
                                                {c.type === "HELP_REQUEST"
                                                    ? "Solicitud de ayuda"
                                                    : "Donación"}
                                                {" · "}{formatDate(c.updatedAt)}
                                            </div>
                                        </div>
                                        <span className="text-muted">→</span>
                                    </div>
                                </div>
                            );
                        })}
                    </div>
                )}
            </div>
        </div>
    );
};

export default ConversationList;