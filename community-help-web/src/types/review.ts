export interface Review {
    id: string;
    authorId: string;
    authorName: string;
    targetId: string;
    targetName: string;
    donationId: string | null;
    helpRequestId: string | null;
    rating: number;
    comment: string | null;
    createdAt: string;
    updatedAt: string;
}