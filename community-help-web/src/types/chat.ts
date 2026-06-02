export interface Conversation {
    id: string;
    type: "HELP_REQUEST" | "DONATION";
    relatedEntityId: string;
    participants: { userId: string; userName: string }[];
    createdAt: string;
    updatedAt: string;
    unreadCount: number;
}

export interface Message {
    id: string;
    conversationId: string;
    senderId: string;
    senderName: string;
    content: string;
    sentAt: string;
}