import { apiClient } from './axios';

// 내 정보 조회
export const getMyProfile = async () => {
    try {
        const response = await apiClient.get('/user/profile/me');

        return response.data;
    } catch (error) {
        console.error('내 정보 조회 api 요청 에러!');
        throw error;
    }
};

// 카카오 로그인
export const oAuth = async (type: string) => {
    window.location.href = import.meta.env.VITE_API_URL + "/user?type=" + type;
};
