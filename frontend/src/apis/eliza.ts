import { apiClient } from "./axios";

export const elizaBoard = async (postId: number) => {
    try {
        const response = await apiClient.post('/lumina/post/' + postId)

        return response.data
    } catch (error) {
        console.error("게시물에 대한 루미나 댓글 생성 API 오류", error);
        throw error
    }
}

export const elizaComment = async (postId: number, commentId: number) => {
    try {
        const response = await apiClient.post('/lumina/post/' + postId + "/comment/" + commentId)

        return response.data
    } catch (error) {
        console.error("댓글에에 대한 루미나 댓글 생성 API 오류", error);
        throw error
    }
}