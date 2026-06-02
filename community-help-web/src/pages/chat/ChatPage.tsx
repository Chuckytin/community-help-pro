import { useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import axios from "axios";
import { toast } from "react-toastify";
import Menubar from "../../components/Menubar";
import { useAppContext } from "../../hooks/useAppContext";
import { useChat } from "../../hooks/useChat";
import { Conversation, Message } from "../../types/chat";

/**
 * Página de chat de una conversación.
 *
 * Flujo:
 * 1. Carga los mensajes históricos por REST al montar
 * 2. Conecta el WebSocket para mensajes en tiempo real
 * 3. Los nuevos mensajes llegan por WS y se añaden al final
 * 4. El usuario envía mensajes por WS (o REST como fallback)
 * 5. El indicador de typing aparece cuando otro usuario escribe
 */
const ChatPage = () => {
    const { id: conversationId } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const { backendURL, token, userData } = useAppContext();

    const [conversation, setConversation] = useState<Conversation | null>(null);
    const [input, setInput] = useState("");
    const [sending, setSending] = useState(false);
    const [loadingHistory, setLoadingHistory] = useState(true);
    const messagesEndRef = useRef<HTMLDivElement>(null);

    const { messages, setMessages, connected, typingUsers, sendMessage, sendTyping } = useChat({
        conversationId: conversationId ?? "",
        token,
        currentUserId: userData?.id ?? "",
    });

    /** Carga el historial de mensajes por REST al montar */
    useEffect(() => {
        if (!conversationId) return;

        const fetchHistory = async () => {
            setLoadingHistory(true);
            try {
                // Carga conversación
                const convs = await axios.get<{ content: Conversation[] }>(
                    `${backendURL}/conversations`, { params: { size: 50 } }
                );
                const conv = convs.data.content.find(c => c.id === conversationId);
                if (conv) setConversation(conv);

                // Carga mensajes históricos
                const { data } = await axios.get<{ content: Message[] }>(
                    `${backendURL}/conversations/${conversationId}/messages`,
                    { params: { size: 100 } }
                );
                // Los mensajes vienen en orden descendente — invertir para mostrar cronológico
                setMessages([...data.content].reverse());

                // Marca como leída
                await axios.patch(
                    `${backendURL}/conversations/${conversationId}/read`
                ).catch(() => { }); // Silencia errores de marca leída
            } catch {
                toast.error("No se pudo cargar el chat.");
                navigate(-1);
            } finally {
                setLoadingHistory(false);
            }
        };

        fetchHistory();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [conversationId, backendURL]);

    /** Scroll al último mensaje cuando llegan nuevos */
    useEffect(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    }, [messages, typingUsers]);

    /**
     * Envía el mensaje.
     * Intenta WebSocket primero — si no está conectado usa REST como fallback.
     */
    const handleSend = async () => {
        if (!input.trim() || !conversationId) return;
        setSending(true);
        try {
            if (connected) {
                sendMessage(input);
                setInput("");
            } else {
                // Fallback REST
                const { data } = await axios.post<Message>(
                    `${backendURL}/conversations/${conversationId}/messages`,
                    { content: input }
                );
                setMessages(prev => [...prev, data]);
                setInput("");
            }
        } catch {
            toast.error("No se pudo enviar el mensaje.");
        } finally {
            setSending(false);
        }
    };

    /** Envía typing al escribir y envía con Enter */
    const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (e.key === "Enter" && !e.shiftKey) {
            e.preventDefault();
            handleSend();
        } else {
            sendTyping();
        }
    };

    const handleDelete = async (messageId: string) => {
        if (!confirm("¿Eliminar este mensaje?")) return;
        try {
            await axios.delete(
                `${backendURL}/conversations/${conversationId}/messages/${messageId}`
            );
            setMessages(prev => prev.filter(m => m.id !== messageId));
        } catch {
            toast.error("No se pudo eliminar el mensaje.");
        }
    };

    const formatTime = (s: string) =>
        new Date(s).toLocaleTimeString("es-ES", {
            hour: "2-digit", minute: "2-digit"
        });

    const formatDate = (s: string) =>
        new Date(s).toLocaleDateString("es-ES", {
            weekday: "short", day: "2-digit", month: "short"
        });

    /**
     * Agrupa mensajes por día para mostrar separadores de fecha.
     * Compara solo la fecha (sin hora) del sentAt.
     */
    const groupedMessages = messages.reduce<{ date: string; msgs: Message[] }[]>(
        (groups, msg) => {
            const date = new Date(msg.sentAt).toDateString();
            const last = groups[groups.length - 1];
            if (last && last.date === date) {
                last.msgs.push(msg);
            } else {
                groups.push({ date, msgs: [msg] });
            }
            return groups;
        }, []
    );

    const otherParticipants = conversation?.participants.filter(
        p => p.userId !== userData?.id
    ) ?? [];

    return (
        <div className="d-flex flex-column min-vh-100" style={{ background: "#f8f9fa" }}>
            <Menubar />

            {/* Cabecera del chat */}
            <div className="bg-white border-bottom shadow-sm px-4 py-3 d-flex align-items-center gap-3">
                <button className="btn btn-sm btn-outline-secondary rounded-pill"
                    onClick={() => navigate("/chat")}>
                    ← Volver
                </button>

                <div className="grow">
                    <div className="d-flex align-items-center gap-2">
                        <span>{conversation?.type === "HELP_REQUEST" ? "🙋" : "🎁"}</span>
                        <span className="fw-semibold">
                            {otherParticipants.map(p => p.userName).join(", ") || "Chat"}
                        </span>
                        {/* Indicador de conexión WebSocket */}
                        <span style={{
                            width: 8, height: 8, borderRadius: "50%",
                            background: connected ? "#28a745" : "#fd7e14",
                        }} title={connected ? "En línea" : "Reconectando..."} />
                    </div>
                    {conversation && (
                        <button
                            className="btn btn-link p-0 text-muted"
                            style={{ fontSize: "0.75rem", textDecoration: "none" }}
                            onClick={() => navigate(
                                conversation.type === "HELP_REQUEST"
                                    ? `/help-requests/${conversation.relatedEntityId}`
                                    : `/donations/${conversation.relatedEntityId}`
                            )}>
                            Ver {conversation.type === "HELP_REQUEST" ? "solicitud" : "donación"} →
                        </button>
                    )}
                </div>
            </div>

            {/* Área de mensajes */}
            <div className="grow overflow-auto px-3 py-3"
                style={{ maxHeight: "calc(100vh - 200px)" }}>
                <div style={{ maxWidth: 640, margin: "0 auto" }}>

                    {loadingHistory ? (
                        <div className="text-center py-5">
                            <div className="spinner-border" style={{ color: "#667eea" }} />
                        </div>
                    ) : messages.length === 0 ? (
                        <div className="text-center py-5 text-muted">
                            <span style={{ fontSize: "2rem" }}>💬</span>
                            <p className="mt-2 small">
                                No hay mensajes todavía. ¡Sé el primero en escribir!
                            </p>
                        </div>
                    ) : (
                        groupedMessages.map(group => (
                            <div key={group.date}>
                                {/* Separador de fecha */}
                                <div className="text-center my-3">
                                    <span className="px-3 py-1 text-muted"
                                        style={{
                                            fontSize: "0.72rem",
                                            background: "#e9ecef",
                                            borderRadius: 20
                                        }}>
                                        {formatDate(group.msgs[0].sentAt)}
                                    </span>
                                </div>

                                {group.msgs.map(msg => {
                                    const isOwn = msg.senderId === userData?.id;
                                    return (
                                        <div key={msg.id}
                                            className={`d-flex mb-2 ${isOwn ? "justify-content-end" : "justify-content-start"}`}>
                                            <div style={{ maxWidth: "75%" }}>
                                                {/* Nombre del remitente (solo mensajes ajenos) */}
                                                {!isOwn && (
                                                    <div className="text-muted mb-1"
                                                        style={{ fontSize: "0.72rem", paddingLeft: 4 }}>
                                                        {msg.senderName}
                                                    </div>
                                                )}

                                                <div className="d-flex align-items-end gap-1"
                                                    style={{ flexDirection: isOwn ? "row-reverse" : "row" }}>

                                                    {/* Burbuja del mensaje */}
                                                    <div style={{
                                                        background: isOwn
                                                            ? "linear-gradient(135deg, #667eea 0%, #764ba2 100%)"
                                                            : "white",
                                                        color: isOwn ? "white" : "#2d3748",
                                                        borderRadius: isOwn
                                                            ? "16px 16px 4px 16px"
                                                            : "16px 16px 16px 4px",
                                                        padding: "8px 14px",
                                                        boxShadow: "0 1px 4px rgba(0,0,0,0.1)",
                                                        wordBreak: "break-word",
                                                        fontSize: "0.9rem",
                                                        lineHeight: 1.5,
                                                    }}>
                                                        {msg.content}
                                                    </div>

                                                    {/* Hora */}
                                                    <div className="text-muted"
                                                        style={{ fontSize: "0.65rem", whiteSpace: "nowrap" }}>
                                                        {formatTime(msg.sentAt)}
                                                    </div>

                                                    {/* Botón eliminar — solo mensajes propios */}
                                                    {isOwn && (
                                                        <button
                                                            className="btn btn-sm p-0 opacity-0 hover-opacity"
                                                            style={{
                                                                fontSize: "0.65rem", color: "#adb5bd",
                                                                lineHeight: 1
                                                            }}
                                                            onClick={() => handleDelete(msg.id)}>
                                                            ✕
                                                        </button>
                                                    )}
                                                </div>
                                            </div>
                                        </div>
                                    );
                                })}
                            </div>
                        ))
                    )}

                    {/* Indicador de typing */}
                    {typingUsers.length > 0 && (
                        <div className="d-flex justify-content-start mb-2">
                            <div style={{
                                background: "white", borderRadius: "16px 16px 16px 4px",
                                padding: "8px 14px", boxShadow: "0 1px 4px rgba(0,0,0,0.1)",
                                fontSize: "0.85rem", color: "#adb5bd"
                            }}>
                                <span style={{ letterSpacing: 2 }}>···</span>
                            </div>
                        </div>
                    )}

                    {/* Ref para scroll automático */}
                    <div ref={messagesEndRef} />
                </div>
            </div>

            {/* Input de mensaje */}
            <div className="bg-white border-top px-3 py-3">
                <div style={{ maxWidth: 640, margin: "0 auto" }}
                    className="d-flex gap-2 align-items-center">
                    <input
                        type="text"
                        className="form-control"
                        style={{
                            borderRadius: 24, border: "1px solid #e2e8f0",
                            padding: "10px 16px", fontSize: "0.9rem"
                        }}
                        placeholder="Escribe un mensaje..."
                        value={input}
                        onChange={e => setInput(e.target.value)}
                        onKeyDown={handleKeyDown}
                        disabled={sending}
                        maxLength={5000}
                    />
                    <button
                        className="btn d-flex align-items-center justify-content-center"
                        style={{
                            width: 42, height: 42, borderRadius: "50%", flexShrink: 0,
                            background: input.trim()
                                ? "linear-gradient(135deg, #667eea 0%, #764ba2 100%)"
                                : "#e9ecef",
                            color: input.trim() ? "white" : "#adb5bd",
                            border: "none", transition: "all 0.2s"
                        }}
                        onClick={handleSend}
                        disabled={!input.trim() || sending}>
                        {sending
                            ? <span className="spinner-border spinner-border-sm" />
                            : "→"}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default ChatPage;