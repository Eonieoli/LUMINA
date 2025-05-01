import { getMyProfile, kakaoAuth } from "@/apis/auth"
import { KakaoAuthButton } from "@/assets/images";
import { useEffect } from "react";
import { useNavigate } from "react-router-dom";

export default function Auth() {
  const navigate = useNavigate();
  useEffect(() => {
      const fetchProfile = async () => {
        try {
          await getMyProfile();
          navigate('/');
        } catch (error) {
          console.error("프로필 불러오기 실패:", error);
        }
      };
  
      fetchProfile();
    }, []);
    
  const handleKakaoAuth = () => {
    kakaoAuth();
  }
  return (
    <div className="w-full h-dvh p-5 bg-pink-200">
      <button className="w-fit h-fit" onClick={handleKakaoAuth}>
        <img className="w-full h-auto" src={KakaoAuthButton} alt="" />
      </button>
    </div>
  )
}