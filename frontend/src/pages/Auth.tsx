import { getMyProfile, kakaoAuth } from '@/apis/auth';
import { KakaoAuthButton } from '@/assets/images';
import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

export default function Auth() {
    const navigate = useNavigate();
    useEffect(() => {
        const fetchProfile = async () => {
            try {
                await getMyProfile();
                navigate('/');
            } catch (error) {
                console.error('프로필 불러오기 실패:', error);
            }
        };

        fetchProfile();
    }, []);

    const handleKakaoAuth = () => {
        kakaoAuth();
    };
    return (
        <div className="h-dvh w-full bg-pink-200 p-5">
            <button className="h-fit w-fit" onClick={handleKakaoAuth}>
                <img className="h-auto w-full" src={KakaoAuthButton} alt="" />
            </button>
        </div>
    );
}
