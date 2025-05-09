import { getMyProfile, oAuth } from '@/apis/auth';
import { GoogleAuthButton, KakaoAuthButton, MainText, MainBackground, MainBackgroundShort } from '@/assets/images';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

export default function Auth() {
    const navigate = useNavigate();
    const [bgImage, setBgImage] = useState(MainBackground); // 기본값을 하나 설정
  
    useEffect(() => {
        const fetchProfile = async () => {
            try {
                await getMyProfile();
                navigate('/');
            } catch (error) {
                console.error('프로필 불러오기 실패:', error);
            }
        };
    
        const handleResize = () => {
            const isMd = window.innerWidth >= 768; // Tailwind md 기준
            setBgImage(isMd ? MainBackground : MainBackgroundShort);
        };
        
        fetchProfile();
        handleResize(); // 초기 렌더링 시 실행
        window.addEventListener('resize', handleResize); // 리사이즈 이벤트 바인딩
  
      return () => {
        window.removeEventListener('resize', handleResize);
      };
    }, []);
  
    const handleOAuth = (type: string) => {
      oAuth(type);
    };

    return (
        <div
            className="flex flex-col justify-between items-center gap-y-20 md:items-start md:justify-center h-dvh w-full p-10 py-20 md:p-24"
            style={{
                backgroundImage: `url(${bgImage})`,
                backgroundSize: 'cover',
                backgroundPosition: 'center',
            }}
        >
            <div>
                <img className='w-2/3 max-h-56 md:max-h-70' src={MainText} alt="" />
            </div>
            <div className='flex flex-col justify-center items-center gap-y-2'>
                <button className="h-fit w-fit" onClick={() => handleOAuth("kakao")}>
                    <img className="h-auto w-full rounded-md border border-gray-200 cursor-pointer" src={KakaoAuthButton} alt="" />
                </button>
                <button className="h-fit w-fit" onClick={() => handleOAuth("google")}>
                    <img className="h-auto w-full rounded-md border border-gray-200 cursor-pointer" src={GoogleAuthButton} alt="" />
                </button>
            </div>
        </div>
    );
}
