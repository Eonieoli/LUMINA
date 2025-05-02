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
export const kakaoAuth = () => {
    const kakaoAuthURL =
        import.meta.env.VITE_KAKAO_AUTH +
        'response_type=code&client_id=' +
        import.meta.env.VITE_KAKAO_REST +
        '&redirect_uri=' +
        import.meta.env.VITE_KAKAO_REDIRECT;
    window.location.href = kakaoAuthURL;
};
