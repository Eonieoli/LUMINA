import { getDonationDetail } from "@/apis/donation"
import { useEffect, useState } from "react"
import { useParams } from "react-router-dom"
import PointInfo from "@/components/donate/PointInfo"
import { BackIcon } from "@/assets/images"

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

  if(!donation) return <div>존재하지 않는 기부처!</div>

  return (
    <div className="w-full h-full px-6 py-6 bg-red-50">
      <img src={BackIcon} alt="뒤로가기" />
      <PointInfo />
      <h1>{donation.donationName}의 상세페이지!!!</h1>
    </div>
  )
}