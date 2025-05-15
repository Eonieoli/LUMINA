import { donationImageMap, defaultDonationThumbnail } from "./DonationImageMap"
import { useNavigate, useParams } from "react-router-dom"

export interface DonationProps {
    donationId: number;
    donationName: string;
}

export default function DonationCard({
    donationId,
    donationName,
}: DonationProps) {

  // 만약 donationId와 일치하는 썸네일이 없다면 디폴트 이미지로 대체
  const donationImg = donationImageMap[donationName] || defaultDonationThumbnail 

  const navigate = useNavigate()

  // 클릭했을 때 상세 페이지로 이동하기
  const goToDonationDetail = () => {
    navigate(`/donate/${donationId}`, {state: {donationName}})
  }

  return (
    <div className="flex flex-col items-center justify-center gap-3 w-full">
      <div className="w-full border border-gray-300 rounded-xl p-2 cursor-pointer flex justify-center items-center" onClick={goToDonationDetail}>
        <img 
          src={donationImg} 
          alt={`${donationName} 이미지`} 
          className="w-50 h-40 object-contain"
        />
      </div>
      <p className="text-[15px] font-medium text-center w-full text-gray-600 truncate">{donationName}</p>

    </div>
  )
}
