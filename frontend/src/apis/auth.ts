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

// 유저 정보 조회
export const getUserProfile = async (userId:number) => {
    try {
        const response = await apiClient.get(`/user/profile/${userId}`)
        // console.log(response.data.data.nickname, "님의 정보 가져오기 성공!", response.data.data)
        return response.data
    }
    catch (error) {
        console.log("유저 정보 가져오기 실패!", error)
    }
}

// 로그인
export const oAuth = async (type: string) => {
    window.location.href = import.meta.env.VITE_API_URL + "/user?type=" + type;
};

// 로그아웃
export const signOut = async () => {
    try {
        const response = await apiClient.post('/user');
        return response.data
    } catch (error) {
        console.error('로그아웃 API 에러', error);
        throw error
    }
}

// 프로필 수정
export const profileEdit = async (formData: FormData) => {
    try{
        const response = await apiClient.patch('/user/profile', formData)
        return response.data.message
    } catch (error: any) {
        console.error("프로필 수정 실패", error)

        const errMsg = error?.response?.data?.message || "프로필 수정 알 수 없는 에러"
        throw new Error(errMsg)
    }
}