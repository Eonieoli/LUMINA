import { useNavigate } from "react-router-dom"
import { BackIcon, CoinIcon, WinkLuna } from "@/assets/images"
import PointInfo from "@/components/donate/PointInfo"
import { useState } from "react"

export default function DonationInputPage() {

  const navigate = useNavigate()
  // const {donationId} = useParams()

  const [point, setPoint] = useState<string>("") // HTML의 input 요소는 항상 문자열로 값을 반환하기 때문에

  // 뒤로가기 버튼 누르기
  const goToBack = () => {
    navigate(-1)
  }

  return (
    <div className="w-full h-full px-6 py-6 mb-20">

      {/* 상단바 */}
      <div className="flex h-6 items-center justify-between mb-4">
        <img src={BackIcon} alt="BackIcon" className="w-5" onClick={goToBack}/>
        <PointInfo/>
      </div>

      <div className="flex flex-col justify-center items-center" >

        {/* 안내 멘트 */}
        <div className="text-[20px] font-medium text-gray-600 mt-20">
          <p>
            기부하실
            <span className="font-bold text-[#5D56F1]">포인트</span>
            <span className="font-bold">를 입력</span>
            해주세요!
          </p>
        </div>

        {/* 루미나 이미지 */}
          <img src={WinkLuna} alt="윙크하는루미나" className="w-60 mt-15 mb-15"/>  
        
        {/* 포인트 입력창 */}
        <div className="flex justify-center items-center w-2/3 pb-0.5 text-gray-400">
          <input 
            type="text" 
            value={point}
            onChange={(e) => {setPoint(e.target.value)}}
            placeholder="100"
            className="text-2xl font-semibold text-center outline-none w-full"
          />
          <img src={CoinIcon} alt="코인" className="w-6" />
        </div>

      </div>
    

    {/* 확인 버튼 */}
    <button 
        className="bg-[#9C97FA] w-full p-3 text-white rounded-2xl text-[20px] font-normal cursor-pointer mt-5 md: md:mt-15 hover:bg-[#5D56F1] transition-colors duration-300 "
        >
        확인
      </button>
    </div>
  )
}