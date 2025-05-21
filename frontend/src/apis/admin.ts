import { apiClient } from './axios';


// 관리자인지 검증
export const amIAdmin = async () => {
    try {
        const response = await apiClient.get('/admin');

        return response.data;
    } catch (error) {
        throw error;
    }
}

// 전체 회원 정보 조회
export const getAllUsers = async (pageNum: number) => {
    try {
        const response = await apiClient.get('/admin/user', {
            params: { pageNum }
        });

        return response.data;
    } catch (error) {
        throw error;
    }
};

// 유저 삭제
export const deleteUser = async (userId: number) => {
    try {
        const response = await apiClient.delete('/admin/user/' + userId);

        return response.data;
    } catch (error) {
        throw error;
    }
}

// 유저 게시물 조회
export const getUserPosts = async (userId: number, pageNum: number) => {
    try {
        const response = await apiClient.get('/admin/post', {
            params: {
                userId,
                pageNum
            }
        })

        return response.data;
    } catch (error) {
        throw error;
    }
}

// 유저 댓글 조회
export const getUserComments = async (userId: number, pageNum: number) => {
    try {
        const response = await apiClient.get('/admin/comment', {
            params: {
                userId,
                pageNum
            }
        })

        return response.data;
    } catch (error) {
        throw error;
    }
}

// 유저 게시물 삭제
export const deletePost = async (postId: number) => {
    try {
        const response = await apiClient.delete('/admin/post/' + postId);

        return response.data;
    } catch (error) {
        throw error;
    }
}

// 유저 댓글 삭제
export const deleteComment = async (postId: number, commentId: number) => {
    try {
        const response = await apiClient.delete('/admin/post/' + postId + '/comment/' + commentId);

        return response.data;
    } catch (error) {
        throw error;
    }
}

// 현재 접속자 조회
export const currentUser = async (pageNum: number) => {
    try {
        const response = await apiClient.get('/admin/cur-user', {
            params: { pageNum }
        })

        return response.data;
    } catch (error) {
        throw error;
    }
}