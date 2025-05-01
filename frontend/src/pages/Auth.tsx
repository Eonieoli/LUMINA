import { kakaoAuth } from "@/apis/auth"
import { KakaoAuthButton } from "@/assets/images";

export default function Auth() {
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