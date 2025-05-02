import { getDonationDetail, toggleDonationSubscribe } from "@/apis/donation"
import { useEffect, useState } from "react"
import { useParams } from "react-router-dom"
import PointInfo from "@/components/donate/PointInfo"
import { BackIcon, HeartDefaultIcon, HeartFilledIcon, CoinIcon } from "@/assets/images"
import { useNavigate } from "react-router-dom"
import { defaultDonationThumbnail, donationImageMap } from "@/components/donate/DonationImageMap"

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

  // const params = useParams()
  // const donationId = params.donationId
  const { donationId } = useParams()
  const [donation, setDonation] = useState<DonationDetail | null>(null)

  // params로 가져온건 string이니까 number로 바꿔주기
  const donaionIdToNum = Number(donationId)
  const donationImg = donationImageMap[donaionIdToNum] || defaultDonationThumbnail

  const navigate = useNavigate()

  useEffect(() => {
    const fetchDetail = async () => {

      // 만약 해당 donationId가 없다!
      if(!donationId) {
        console.log("useParams 값:", donationId)
        return
      } 

      try {
        const response = await getDonationDetail(donationId)
        setDonation(response)
      }
      catch(error) {
      }
    }
    fetchDetail()
  },[donationId])

  // 뒤로가기 버튼을 클릭했을 때
  const goToBack = () => {
    navigate(-1)
  }

  // 구독하기 버튼을 클릭했을 때
  const handleToggleSubscribe = async () => {
    if(!donation) return
    try {
      await toggleDonationSubscribe(donation.donationId)
      setDonation(prev => prev ? {...prev, isSubscribe: !prev.isSubscribe}: null)
    }
    catch (error){}
  }

  // 기부하기 버튼을 클릭했을 때
  const goToDonate = () => {
    navigate(`/donate/${donation?.donationId}/point`)
  }

  if(!donation) return <div>존재하지 않는 기부처!</div>

  return (
    <div className="w-full h-full px-6 py-6">

      {/* 상단바 */}
      <div className="flex h-6 items-center justify-between mb-4">
        <img src={BackIcon} alt="BackIcon" className="w-5" onClick={goToBack} />
        <PointInfo />
      </div>

      {/* 기부처 상단 정보 */}
      <div className="flex flex-col justify-center items-center mb-6">

        <div className="h-[150px] w-[260px] overflow-hidden flex items-center justify-center mb-2">
          <img src={donationImg} alt="" className="max-w-full max-h-full object-contain"/>
        </div>

        <div className="mb-2 flex items-center">
          <p className="text-xl font-bold text-gray-600 mr-2">{donation.donationName}</p>
          <div>
            {donation.isSubscribe ? 
              <img src={HeartFilledIcon} alt="구독중" onClick={handleToggleSubscribe} />
              :
              <img src={HeartDefaultIcon} alt="구독안함" onClick={handleToggleSubscribe}/>
            }
          </div>
        </div>
        <p className="text-[16px] text-gray-600 text-center w-[260px]">생명에 대한 존중과 차별없는 동물권을 지향하는 시민들의 모임</p>

      </div>

      {/* 기부처 하단 정보 */}
      <div className="w-full border-gray-200 rounded-2xl p-5 border-2 text-gray-600 font-medium text-[17px]">
        
        <div className="flex justify-between">
          <p className="font-semibold">총 기부 금액 </p>
          <div className="flex">
            <p className="text-[#5D56F1]">{donation.sumPoint}</p>
            <p>&nbsp;/&nbsp;100</p>
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

      {/* 기부하기 버튼*/}
      <button 
        className="bg-[#9C97FA] w-full p-3 text-white rounded-2xl text-[20px] font-normal cursor-pointer mt-5 md:mt-10 hover:bg-[#5D56F1] transition-colors duration-300 "
        onClick={goToDonate}        
        >
        기부하기
      </button>
      
    </div>
  )
}