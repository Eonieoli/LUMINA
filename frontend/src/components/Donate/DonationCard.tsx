import { donationImageMap, defaultDonationThumbnail } from "./DonationImageMap"

interface DonationProps {
  donationId: number
  donationName: string
}

export default function DonationCard({ donationId, donationName} : DonationProps) {

  // 만약 donationId와 일치하는 썸네일이 없다면 디폴트 이미지로 대체
  const donationImg = donationImageMap[donationId] || defaultDonationThumbnail 

  return (
    <div className="flex flex-col items-center justify-center gap-3 w-full">
      <div className="w-full border border-gray-300 rounded-xl p-2 cursor-pointer flex justify-center items-center">
        <img 
          src={donationImg} 
          alt={`${donationName} 이미지`} 
          className="w-50 h-40 object-contain"
        />
      </div>
      <p className="text-l font-medium text-center text-gray-600">{donationName}</p>

    </div>
  )
}