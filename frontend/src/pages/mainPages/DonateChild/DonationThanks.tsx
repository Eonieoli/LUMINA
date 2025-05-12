import { useEffect, useState } from "react";
import DonationLayout from "./DonationLayout";
import { ComputerHandsupLuna } from "@/assets/images";
import { getPointInfo } from "@/apis/donation";
import { useNavigate } from "react-router-dom";

export default function DonationThanks () {

  const [nickname, setNickkname] = useState<string>("Lumina")
  const [userId, setUserId] = useState<number>(0)
  const navigate = useNavigate()

  useEffect(() => {
    const fetchNicknameInfo = async () => {
      const response = await getPointInfo()
      setNickkname(response.nickname)
      setUserId(response.userId)
    }
    fetchNicknameInfo()
  },[])

  return (
    <DonationLayout
      bottomButton = {
        <button 
          className="w-full h-full relative p-3"
          //기부처 디테일 페이지(뒤로가기 문제)로 이동하게 할건지 마이페이지로 이동하게 할건지 고민
          // onClick={() => {
          //   if (donationId) navigate(`/donate/${donationId}`);
          // }}
          onClick={() => navigate(`/mypage/${userId}`)}
        >
          확인
        </button>
      }
    >
        <div className="flex flex-col h-full items-center justify-center">
          <div className="text-[20px]">
            {nickname}님의
            <span className="font-bold">
              <span className="text-[#5D56F1]"> 기부</span>
              에 감사
            </span>
            드립니다.
          </div>
          <img src={ComputerHandsupLuna} alt="루나 이미지" className="w-1/2 mt-10" />
        </div>
    </DonationLayout>
  )
}