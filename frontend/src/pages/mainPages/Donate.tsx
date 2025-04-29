import { useState, useEffect } from "react"
import DonateSearchBar from "@/components/Donate/SearchBar"
import FavoriteDonationList from "@/components/Donate/FavoriteDonationList"
import RecommendDonationList from "@/components/Donate/RecommendDonationList"
import PointInfo from "@/components/Donate/PointInfo"

export default function DonatePage() {

  const mockData = [
    { donationId: 101, donationName: "기부처 A" },
    { donationId: 102, donationName: "기부처 B" },
    { donationId: 103, donationName: "기부처 C" },
    { donationId: 104, donationName: "기부처 D" },
    { donationId: 105, donationName: "기부처 F"}
  ]

  return (
    <div className="flex flex-col w-full px-6 py-6">

      {/* 코인 조회 */}
        <PointInfo />

      {/* 검색창 */}
      <div className="w-full mb-8">
        <DonateSearchBar/>
      </div>

      {/* 추천 기부처 */}
      <div className="w-full mb-6">
        <RecommendDonationList donations={mockData}/>
      </div>

      {/* 관심 기부처 */}
      <div className="w-full mb-6">
        <FavoriteDonationList/>
      </div>
    </div>
  )
}