import DonationCard from "./DonationCard"
import { PokerLuna } from "@/assets/images";

interface RecommendDonationListProps {
  donations: { donationId: number; donationName:string}[]
}

export default function RecommendDonationList({ donations}: RecommendDonationListProps) {
  return (
    <div className="flex flex-col gap-4">

      {/* 추천 기부처 텍스트 영역 */}
      <div className="flex flex-col items-start gap-2">

        <div className="flex items-center gap-3">
          <img src={PokerLuna} alt="추천 기부처" className="w-12 justufy-center" />
          <p className="text-2xl font-bold">이런 곳은 어때요?</p>
        </div>
        <p className="text-xl text-gray-600">AI가 여러분의 관심에 따라 기부처를 추천해드릴게요!</p>
      </div>

      {/* 추천 기부처 목록 */}
      <div className="grid grid-cols-2 gap-4">
        {donations.map((donation) => (
          <DonationCard
            key={donation.donationId}
            donationId={donation.donationId}
            donationName={donation.donationName}
          />
        ))}
      </div>
    </div>
  )
}