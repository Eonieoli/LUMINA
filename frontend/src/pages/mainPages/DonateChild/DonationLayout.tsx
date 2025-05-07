import { BackIcon } from "@/assets/images";
import PointInfo from "@/components/donate/PointInfo";
import { ReactNode } from "react";
import { useNavigate } from "react-router-dom";

interface DonationLayoutProps {
  children: ReactNode
  bottomButton?: ReactNode
  isModalOpen?: Boolean
}

export default function DonationLayout({children, bottomButton, isModalOpen}: DonationLayoutProps) {

  const navigate = useNavigate()

  // 뒤로가기를 클릭했을 때
const goToBack = () => {
  navigate(-1)
}

  return(
    <div  className={`relative w-full h-full px-6 pt-6 flex flex-col ${isModalOpen ? "bg-black/10" : ""}`}>

      {/* 공통 상단바 */}
      <div className="flex items-center justify-between mb-6 h-6">
        <img 
          src={BackIcon} alt="뒤로가기" 
          className="w-5"
          onClick={goToBack}
        />
        <PointInfo />
      </div>

      {/* 콘텐츠 영역 */}
      <div className="flex-1 overflow-y-auto">
        {children}
      </div>

      {/* 하단 버튼 */}
      {bottomButton && (
        <div className="bg-[#9C97FA] w-full mb-6 text-white rounded-2xl text-[20px] font-normal cursor-pointer hover:bg-[#5D56F1] transition-colors duration-300 text-center mt-6">
          {bottomButton}
        </div>
      )}
    </div>
  )
}