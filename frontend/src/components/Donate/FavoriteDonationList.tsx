import DonationCard from "./DonationCard"
import { SmileLuna } from "@/assets/images";

interface FavoriteDonationListProps {
  donations: { donationId: number; donationName:string}[]
}

export default function FavoriteDonationList({ donations}: FavoriteDonationListProps) {
  return (
    <div className="flex flex-col gap-4">

      {/* 관심기부처 텍스트 영역 */}
      <div className="flex flex-col items-start gap-2">

        <div className="flex items-center gap-3">
          <img src={SmileLuna} alt="관심있는 기부처" className="w-12 justufy-center" />
          <p className="text-2xl font-bold">관심있는 기부처</p>
        </div>
        <p className="text-xl text-gray-600">여러분의 따뜻한 선행이 세상을 바꿔요!</p>
      </div>

      {/* 관심기부처 목록 */}
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