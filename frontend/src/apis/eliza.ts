import { apiClient } from "./axios";
import { logApiEvent } from '@/utils/analytics';

export const elizaBoard = async (postId: number) => {
    try {
        const response = await apiClient.post('/lumina/post/' + postId)

        logApiEvent("elizaBoard", "success");
        return response.data
    } catch (error) {
        console.error("게시물에 대한 루미나 댓글 생성 API 오류", error);
        logApiEvent("elizaBoard", "error");
        throw error
    }
}

export const elizaComment = async (postId: number, commentId: number) => {
    try {
        const response = await apiClient.post('/lumina/post/' + postId + "/comment/" + commentId)

        logApiEvent("elizaComment", "success");
        return response.data
    } catch (error) {
        console.error("댓글에에 대한 루미나 댓글 생성 API 오류", error);
        logApiEvent("elizaComment", "error");
        throw error
    }
}