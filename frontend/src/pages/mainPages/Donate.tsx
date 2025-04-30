import { useState } from "react"
import DonateSearchBar from "@/components/donate/SearchBar"
import FavoriteDonationList from "@/components/donate/FavoriteDonationList"
import RecommendDonationList from "@/components/donate/RecommendDonationList"
import PointInfo from "@/components/donate/PointInfo"
import SearchModal from "@/components/donationSearch/SearchModal"


export default function DonatePage() {

  // 추천 기부페이지 임시 데이터
  const mockData = [
    { donationId: 101, donationName: "기부처 A" },
    { donationId: 102, donationName: "기부처 B" },
    { donationId: 103, donationName: "기부처 C" },
    { donationId: 104, donationName: "기부처 D" },
    { donationId: 105, donationName: "기부처 F"}
  ]

  // 검색창 버튼 클릭시 보일 검색 모달창 상태 관리
  const [isSearchOpen, setIsSearchOpen] = useState(false)

  return (
    <div className="relative flex flex-col w-full px-6 py-6 bg-red-100">

      {/* 검색 모달창 */}
      {isSearchOpen && <SearchModal onClose={() => setIsSearchOpen(false)} />}

      {/* 코인 조회 */}
      <PointInfo />

      {/* 검색창 */}
      <div className="w-full mb-8" onClick={() => setIsSearchOpen(true)}>
        <DonateSearchBar
          keyword=""
          setKeyword={() => {}}
          onSearchClick={()=>{}}
        />
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