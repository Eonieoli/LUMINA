import { apiClient } from './axios';

// 게시물 등록
interface CreatePostParams {
    postImageFile?: File | null;
    hashtag: string[];
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

        return response.data;
    } catch (error) {
        console.error('게시물 생성 API 요청 에러:', error);
        throw error;
    }
};

// 게시물 전체 조회 페이지네이션
export const getPosts = async (pageNum: number, feedType: string) => {
    try {
        const response = await apiClient.get('/post', {
            params: { pageNum, feedType },
        });

        return response.data;
    } catch (error) {
        console.error('게시물 전체 조회 API 요청 에러 : ', error);
        throw error;
    }
};

// 특정 유저 게시물 전체 조회
export const getUserPosts = async (userId: number, pageNum: number) => {
    try {
        const response = await apiClient.get('/post', {
            params: {userId, pageNum},
        })
        return response.data.data
    }
    catch (error) {
        console.log("특정 유저 게시물 조회 실패!", error)
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

        return response.data;
    } catch (error) {
        console.error('댓글 조회 API 요청 에러 : ', error);
        throw error;
    }
};

// 게시물 대댓글 조회
export const getReplies = async (postId: number, commentId: number) => {
    try {
        const response = await apiClient.get(
            '/post/' + postId + '/comment/' + commentId
        );

        return response.data;
    } catch (error) {
        console.error('대댓글 조회 API 요청 에러 : ', error);
        throw error;
    }
};

// 게시물 삭제
export const deletePost = async (postId: number) => {
    try {
        const response = await apiClient.delete('/post/' + postId);

        return response.data;
    } catch (error) {
        console.error('게시물 삭제 API 요청 에러 : ', error);
        throw error;
    }
};

// 게시물 좋아요
export const postLike = async (postId: number) => {
    try {
        const response = await apiClient.post('/post/' + postId + '/like');

        return response.data;
    } catch (error) {
        console.error('게시물 좋아요 API 요청 에러 : ', error);
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
        return response.data;
    } catch (error) {
        console.error('게시물 댓글 등록 API 요청 에러: ', error);
        throw error;
    }
};

// 댓글 삭제
export const deleteComment = async (postId: number, commentId: number) => {
    try {
        const response = await apiClient.delete(
            '/post/' + postId + '/comment/' + commentId
        );

        return response.data;
    } catch (error) {
        console.error('게시물 댓글 삭제 API 요청 에러: ', error);
        throw error;
    }
};

// 댓글 좋아요
export const commentLike = async (postId: number, commentId: number) => {
    try {
        const response = await apiClient.post(
            '/post/' + postId + '/comment/' + commentId + '/like'
        );

        return response.data;
    } catch (error) {
        console.error('게시물 댓글 삭제 API 요청 에러: ', error);
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
        return response.data
    } catch (error) {
        console.error("카테고리 검색 API 요청 에러: ", error);
        throw error
    }
}

// 전체 카테고리 조회
export const getCategories = async () => {
    try {
        const response = await apiClient.get('/category');
        return response.data
    } catch (error) {
        console.error("카테고리 전체 조회회 API 에러: ", error);
        throw error
    }
}

// 카테고리 구독
export const subscribeCategory = async (categoryId: number) => {
    try {
        const response = await apiClient.post('/category/' + categoryId);
        return response.data
    } catch (error) {
        console.error("카테고리 구독 요청 API 에러: ", error);
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

        return response.data
    } catch (error) {
        console.error('유저 검색 API 요청 에러: ', error);
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

        return response.data
    } catch (error) {
        console.error('해시태그 게시물 조회 API 요청 에러: ', error);
        throw error
    }
}