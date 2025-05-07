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
        console.log(response.data.data.nickname, "님의 정보 가져오기 성공!", response.data.data)
        return response.data
    }
    catch (error) {
        console.log("유저 정보 가져오기 실패!", error)
    }
}

// 카카오 로그인
export const oAuth = async (type: string) => {
    window.location.href = import.meta.env.VITE_API_URL + "/user?type=" + type;
};
