import { useEffect, useRef, useState, useCallback } from "react";
import { Client } from "@stomp/stompjs";
import { Message } from "../types/chat";

interface UseChatOptions {
    conversationId: string;
    token: string | null;
    currentUserId: string;
}

/**
 * Hook para el chat en tiempo real.
 *
 * Gestiona:
 * - Conexión STOMP sobre WebSocket con autenticación JWT
 * - Suscripción al canal de mensajes de la conversación
 * - Suscripción al canal de typing
 * - Envío de mensajes y eventos de typing
 *
 * El JWT se pasa como query param en la URL del WebSocket:
 *   ws://localhost:8080/ws?token=eyJ...
 * Esto es lo que espera el backend en ChatWebSocketController.
 */
export const useChat = ({ conversationId, token, currentUserId }: UseChatOptions) => {
    const [messages, setMessages] = useState<Message[]>([]);
    const [connected, setConnected] = useState(false);
    const [typingUsers, setTypingUsers] = useState<string[]>([]);
    const clientRef = useRef<Client | null>(null);

    useEffect(() => {
        if (!token || !conversationId) return;

        // Crea el cliente STOMP
        const client = new Client({
            /**
             * URL del WebSocket con el JWT como query param.
             * El backend extrae el token de la sesión al conectar.
             */
            brokerURL: `ws://localhost:8080/ws?token=${token}`,
            reconnectDelay: 5000,

            onConnect: () => {
                setConnected(true);

                // Suscripción a mensajes de la conversación
                client.subscribe(
                    `/topic/conversations/${conversationId}`,
                    (frame) => {
                        const msg: Message = JSON.parse(frame.body);
                        setMessages(prev => {
                            // Evita duplicados si el mensaje ya llegó por REST
                            if (prev.find(m => m.id === msg.id)) return prev;
                            return [...prev, msg];
                        });
                    }
                );

                // Suscripción al indicador de typing
                client.subscribe(
                    `/topic/conversations/${conversationId}/typing`,
                    (frame) => {
                        const userId: string = JSON.parse(frame.body);
                        // No mostramos el propio typing
                        if (userId === currentUserId) return;

                        setTypingUsers(prev =>
                            prev.includes(userId) ? prev : [...prev, userId]
                        );
                        // Elimina el indicador tras 3 segundos
                        setTimeout(() => {
                            setTypingUsers(prev => prev.filter(id => id !== userId));
                        }, 3000);
                    }
                );
            },

            onDisconnect: () => setConnected(false),
            onStompError: (frame) => {
                console.error("[Chat] STOMP error:", frame.headers["message"]);
            },
        });

        client.activate();
        clientRef.current = client;

        return () => {
            client.deactivate();
            clientRef.current = null;
            setConnected(false);
        };
    }, [conversationId, token, currentUserId]);

    /**
     * Envía un mensaje por WebSocket.
     * Destino: /app/chat.sendMessage
     */
    const sendMessage = useCallback((content: string) => {
        if (!clientRef.current?.connected || !content.trim()) return;
        clientRef.current.publish({
            destination: "/app/chat.sendMessage",
            body: JSON.stringify({ conversationId, content: content.trim() }),
        });
    }, [conversationId]);

    /**
     * Notifica que el usuario está escribiendo.
     * Destino: /app/chat.typing
     */
    const sendTyping = useCallback(() => {
        if (!clientRef.current?.connected) return;
        clientRef.current.publish({
            destination: "/app/chat.typing",
            body: JSON.stringify({ conversationId, typing: true }),
        });
    }, [conversationId]);

    return { messages, setMessages, connected, typingUsers, sendMessage, sendTyping };
};