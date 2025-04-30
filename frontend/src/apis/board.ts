import { apiClient } from "./axios";


// 게시물 전체 조회 페이지네이션
export const getPosts = async (pageNum: number) => {
    try {
        const response = await apiClient.get("/post", {
            params: { pageNum },
        });

        return response.data;
    } catch (error) {
        console.log("게시물 전체 조회 API 요청 에러 : ", error);
        throw error;
    }
};


// 게시물 댓글 조회
// 게시물 피드에서 댓글 클릭 시
export const getComments = async (pageNum: number, postId: number) => {
    try {
        const response = await apiClient.get("/post/" + postId + "/comment", {
            params: { pageNum },
        });

        return response.data;
    } catch (error) {
        console.log("댓글 조회 API 요청 에러 : ", error);
        throw error;
    }
};

// 게시물 대댓글 조회
export const getReplies = async (postId: number, commentId: number) => {
    try {
        const response = await apiClient.get("/post/" + postId + "/comment/" + commentId);

        return response.data;
    } catch (error) {
        console.log("대댓글 조회 API 요청 에러 : ", error);
        throw error;
    }
};

// 게시물 삭제
export const deletePost = async (postId: number) => {
    try {
        const response = await apiClient.delete("/post/" + postId);

        return response.data;
    } catch (error) {
        console.log("게시물 삭제 API 요청 에러 : ", error);
        throw error;
    }
};

// 게시물 좋아요
export const postLike = async (postId: number) => {
    try {
        const response = await apiClient.post("/post/" + postId + "/like");

        return response.data;
    } catch (error) {
        console.log("게시물 좋아요 API 요청 에러 : ", error);
        throw error;
    }
}

// 게시물 댓글 등록
// 대댓글인 경우 commentId 추가
export const postComment = async (postId: number, content: string, commentId?: number) => {
    try {
        const data: { commentContent: string; parentCommentId?: number } = {
            commentContent: content,
        };

        if (commentId) {
            data.parentCommentId = commentId;
        }

        const response = await apiClient.post(`/post/${postId}/comment`, data);
        return response.data;
    } catch (error) {
        console.log("게시물 댓글 등록 API 요청 에러: ", error);
        throw error;
    }
};

// 댓글 삭제
export const deleteComment = async (postId: number, commentId: number) => {
    try {
        const response = await apiClient.delete("/post/" + postId + "/comment/" + commentId);

        return response.data;
    } catch (error) {
        console.log("게시물 댓글 삭제 API 요청 에러: ", error);
        throw error;
    }
};

// 댓글 좋아요
export const commentLike = async (postId: number, commentId: number) => {
    try {
        const response = await apiClient.post("/post/" + postId + "/comment/" + commentId + "/like");

        return response.data;
    } catch (error) {
        console.log("게시물 댓글 삭제 API 요청 에러: ", error);
        throw error;
    }
}