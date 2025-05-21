import { apiClient } from './axios';
import { logApiEvent } from '@/utils/analytics';

// 내 정보 조회
export const getMyProfile = async () => {
    try {
        const response = await apiClient.get('/user/profile/me');

        logApiEvent("getMyProfile", "success");
        return response.data;
    } catch (error) {
        logApiEvent("getMyProfile", "error");
        throw error;
    }
};

// 유저 정보 조회
export const getUserProfile = async (userId:number) => {
    try {
        const response = await apiClient.get(`/user/profile/${userId}`)
        logApiEvent("getUserProfile", "success");
        return response.data
    }
    catch (error) {
        logApiEvent("getUserProfile", "error");
        throw error;
    }
}

// 로그인
export const oAuth = async (type: string) => {
    window.location.href = import.meta.env.VITE_API_URL + "/user?type=" + type;
};

// 로그아웃
export const signOut = async () => {
    try {
        const response = await apiClient.post('/user/logout');
        logApiEvent("signOut", "success");
        return response.data
    } catch (error) {
        logApiEvent("signOut", "error");
        throw error
    }
}

// 프로필 수정
export const profileEdit = async (formData: FormData) => {
    try{
        const response = await apiClient.patch('/user/profile', formData)
        logApiEvent("profileEdit", "success");
        return response.data.message
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (error: any) {
        logApiEvent("profileEdit", "error");
        
        const errMsg = error?.response?.data?.message || "프로필 수정 알 수 없는 에러"
        throw new Error(errMsg)
    }
}