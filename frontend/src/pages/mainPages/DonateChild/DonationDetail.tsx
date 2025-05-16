import { getDonationDetail, toggleDonationSubscribe } from "@/apis/donation"
import { useEffect, useState } from "react"
import { useParams, useLocation } from "react-router-dom"
import { HeartDefaultIcon, HeartFilledIcon, CoinIcon } from "@/assets/images"
import { useNavigate } from "react-router-dom"
import { defaultDonationThumbnail, donationImageMap } from "@/components/donate/DonationImageMap"
import DonationLayout from "./DonationLayout"
import donationDescriptionMap from "@/components/donate/DonationDescriptionMap"

interface DonationDetail {
  donationId: number
  donationName: string
  sumPoint: number
  sumUser: number
  myDonationCnt: number
  mySumDonation: number
  isSubscribe: boolean
}

export default function DonationDetailPage() {

  const { donationId } = useParams()
  const location = useLocation()
  const {donationName: donationNameFromState} = location.state || {}

  const [donation, setDonation] = useState<DonationDetail | null>(null)

  const donationImg = donationImageMap[donationNameFromState] || defaultDonationThumbnail

  const navigate = useNavigate()

  useEffect(() => {
    const fetchDetail = async () => {

      // 만약 해당 donationId가 없다!
      if(!donationId) {
        console.log("useParams 값:", donationId)
        return
      } 
      const response = await getDonationDetail(donationId)
      setDonation(response)
    }
    fetchDetail()
  },[donationId])

  // 구독하기 버튼을 클릭했을 때
  const handleToggleSubscribe = async () => {
    if(!donation) return
    await toggleDonationSubscribe(donation.donationId)
    setDonation(prev => prev ? {...prev, isSubscribe: !prev.isSubscribe}: null)
  }

  // 기부하기 버튼을 클릭했을 때
  const goToDonate = () => {
    console.log(`${donation?.donationName}`,"으로 기부하러 이동")
    navigate(`/donate/${donation?.donationId}/point`)
  }

  if(!donation) return <div>존재하지 않는 기부처!</div>

  return (

    <DonationLayout
      bottomButton = {
        <button onClick={goToDonate} className="w-full h-full p-3">기부하기</button>
      }
    >
      <div className="w-full flex flex-col justify-center">
        {/* 기부처 상단 정보 */}
        <div className="flex flex-col justify-center items-center mb-6">

          <div className="h-[150px] w-[260px] overflow-hidden flex items-center justify-center mb-2">
            <img src={donationImg} alt="" className="max-w-full max-h-full object-contain"/>
          </div>

          <div className="mb-2 flex items-center">
            <p className="text-xl font-bold text-gray-600 mr-2">{donation.donationName}</p>
            <div className="cursor-pointer">
              {donation.isSubscribe ? 
                <img src={HeartFilledIcon} alt="구독중" onClick={handleToggleSubscribe} />
                :
                <img src={HeartDefaultIcon} alt="구독안함" onClick={handleToggleSubscribe}/>
              }
            </div>
          </div>
          <p className="text-[16px] text-gray-600 text-center">
            {donationDescriptionMap[donation.donationName]}
          </p>

        </div>

        {/* 기부처 하단 정보 */}
        <div className="w-full border-gray-200 rounded-2xl p-5 border-2 text-gray-600 font-medium text-[17px] bg-red">
          
          <div className="flex justify-between">
            <p className="font-semibold">총 기부 금액 </p>
            <div className="flex">
              <p className="text-[#5D56F1] mr-1">{donation.sumPoint}</p>
              <img src={CoinIcon} alt="포인트이미지" className="w-4 object-contain "/>
            </div>
          </div>

          <div className="flex justify-between">
            <p className="font-semibold">누적 기부자 수</p>
            <p>{donation.sumUser}명</p>
          </div>

          <div className="flex justify-between mt-10">
            <p className="font-semibold">나의 기부 횟수</p>
            <p>{donation.myDonationCnt}회</p>
          </div>

          <div className="flex justify-between">
            <p className="font-semibold">나의 기부 누적금액</p>
            <div className="flex items-center">
              <p className="mr-1">{donation.mySumDonation}</p>
              <img src={CoinIcon} alt="포인트이미지" className="w-4 object-contain "/>
            </div>
          </div>

        </div>
      </div>
      
    </DonationLayout>
  )
}