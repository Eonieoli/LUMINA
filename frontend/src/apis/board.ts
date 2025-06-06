import { apiClient } from './axios';
import { logApiEvent } from '@/utils/analytics';

// 게시물 등록
interface CreatePostParams {
    postImageFile?: File | null;
    hashtag: string;
    postContent: string;
}

export const createPost = async ({
    postImageFile,
    hashtag,
    postContent,
}: CreatePostParams) => {
    try {
        const formData = new FormData();

        if (postImageFile) {
            formData.append('postImageFile', postImageFile);
        }
        formData.append('hashtag', JSON.stringify(hashtag));
        formData.append('postContent', postContent);

        const response = await apiClient.post('/post', formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });

        logApiEvent("createPost", "success");
        return response.data;
    } catch (error) {
        logApiEvent("createPost", "error");
        throw error;
    }
};

// 게시물 전체 조회 페이지네이션
export const getPosts = async (pageNum: number, feedType: string) => {
    try {
        const response = await apiClient.get('/post', {
            params: { pageNum, feedType },
        });

        logApiEvent("getPosts", "success");
        return response.data;
    } catch (error) {
        logApiEvent("getPosts", "error");
        throw error;
    }
};

// 특정 유저 게시물 전체 조회
export const getUserPosts = async (userId: number, pageNum: number) => {
    try {
        const response = await apiClient.get('/post', {
            params: {userId, pageNum},
        })
            
        logApiEvent("getUserPosts", "success");
        return response.data.data
    }
    catch (error) {
        logApiEvent("getUserPosts", "error");
        throw error
    }
}

// 게시물 댓글 조회
// 게시물 피드에서 댓글 클릭 시
export const getComments = async (pageNum: number, postId: number) => {
    try {
        const response = await apiClient.get('/post/' + postId + '/comment', {
            params: { pageNum },
        });

        logApiEvent("getComments", "success");
        return response.data;
    } catch (error) {
        logApiEvent("getComments", "error");
        throw error;
    }
};

// 게시물 대댓글 조회
export const getReplies = async (postId: number, commentId: number) => {
    try {
        const response = await apiClient.get(
            '/post/' + postId + '/comment/' + commentId
        );

        logApiEvent("getReplies", "success");
        return response.data;
    } catch (error) {
        logApiEvent("getReplies", "error");
        throw error;
    }
};

// 게시물 삭제
export const deletePost = async (postId: number) => {
    try {
        const response = await apiClient.delete('/post/' + postId);

        logApiEvent("deletePost", "success");
        return response.data;
    } catch (error) {
        logApiEvent("deletePost", "error");
        throw error;
    }
};

// 게시물 좋아요
export const postLike = async (postId: number) => {
    try {
        const response = await apiClient.post('/post/' + postId + '/like');

        logApiEvent("postLike", "success");
        return response.data;
    } catch (error) {
        logApiEvent("postLike", "error");
        throw error;
    }
};

// 게시물 댓글 등록
// 대댓글인 경우 commentId 추가
export const postComment = async (
    postId: number,
    content: string,
    commentId?: number
) => {
    try {
        const data: { commentContent: string; parentCommentId?: number } = {
            commentContent: content,
        };

        if (commentId) {
            data.parentCommentId = commentId;
        }

        const response = await apiClient.post(`/post/${postId}/comment`, data);
        logApiEvent("postComment", "success");
        return response.data;
    } catch (error) {
        logApiEvent("postComment", "success");
        throw error;
    }
};

// 댓글 삭제
export const deleteComment = async (postId: number, commentId: number) => {
    try {
        const response = await apiClient.delete(
            '/post/' + postId + '/comment/' + commentId
        );

        logApiEvent("deleteComment", "success");
        return response.data;
    } catch (error) {
        logApiEvent("deleteComment", "error");
        throw error;
    }
};

// 댓글 좋아요
export const commentLike = async (postId: number, commentId: number) => {
    try {
        const response = await apiClient.post(
            '/post/' + postId + '/comment/' + commentId + '/like'
        );

        logApiEvent("commentLike", "success");
        return response.data;
    } catch (error) {
        logApiEvent("commentLike", "error");
        throw error;
    }
};

// 카테고리 검색
export const getCategoryExplore = async (pageNum: number, ) => {
    try {
        const response = await apiClient.get('/post/category', {
            params: {
                pageNum
            }
        })
        logApiEvent("getCategoryExplore", "success");
        return response.data
    } catch (error) {
        logApiEvent("getCategoryExplore", "error");
        throw error
    }
}

// 전체 카테고리 조회
export const getCategories = async () => {
    try {
        const response = await apiClient.get('/category');
        logApiEvent("getCategories", "success");
        return response.data
    } catch (error) {
        logApiEvent("getCategories", "success");
        throw error
    }
}

// 카테고리 구독
export const subscribeCategory = async (categoryId: number) => {
    try {
        const response = await apiClient.post('/category/' + categoryId);
        logApiEvent("subscribeCategory", "success");
        return response.data
    } catch (error) {
        logApiEvent("subscribeCategory", "success");
        throw error
    }
}

// 유저명으로 유저 검색
export const getUser = async (keyword: string) => {
    try {
        const response = await apiClient.get('/user/search', {
            params: {
                keyword,
                pageNum: 1
            }
        })

        logApiEvent("getUser", "success");
        return response.data
    } catch (error) {
        logApiEvent("getUser", "error");
        throw error
    }
}

// 해시태그로 게시물 조회
export const getHashtagPosts = async (keyword: string, pageNum: number) => {
    try {
        const response = await apiClient.get('/post/search', {
            params: {
                keyword,
                pageNum
            }
        })

        logApiEvent("getHashtagPosts", "success");
        return response.data
    } catch (error) {
        logApiEvent("getHashtagPosts", "error");
        throw error
    }
}