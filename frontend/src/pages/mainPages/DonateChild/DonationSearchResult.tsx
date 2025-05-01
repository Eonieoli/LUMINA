import { useEffect, useState } from "react"
import SearchModal from "@/components/donationSearch/SearchModal"
import PointInfo from "@/components/donate/PointInfo"
import DonateSearchBar from "@/components/donate/SearchBar"
import { useParams } from "react-router-dom"
import { getSearchDonations } from "@/apis/donation"
import DonationCard from "@/components/donate/DonationCard"
import { DonationProps } from "@/components/donate/DonationCard"
import { DownIcon } from "@/assets/images"

export default function DonationSearchResultPage() {

  const [isSearchOpen, setIsSearchOpen] = useState(false)
  
  const {keyword} = useParams()
  const decodedKeyword = keyword === "donations" ? "" : decodeURIComponent(keyword || "")
  const [donations, setDonations] = useState<DonationProps[]>([])
  const [pageNum, setPageNum] = useState(1)
  const [totalPages, setTotalPages] = useState(1)

  // 검색어가 변경이 되면 페이지 번호를 1로 초기화
  useEffect(() => {
    setPageNum(1)
    setDonations([])
  },[decodedKeyword])

  useEffect(() => {
    const fetchDonations = async() => {
      if(!keyword || keyword === "donations") 
        console.log("빈칸 검색 -> 전체 목록 조회")
      try {
        const { donations: newDonations, totalPages} = await getSearchDonations(decodedKeyword, pageNum)
        if (pageNum === 1) {
          setDonations(newDonations)
        }
        else {
          // 중복된 항목들은 제거하구 새로운 항목만 추가하기!!
          setDonations ((prev) => {
            // 원래 있었던 기부처들
            const existingIds = prev.map(donation => donation.donationId)
            // 페이지네이션으로 추가되는 기부처들
            const uniqueNewDonations = newDonations.filter(
              (donation:DonationProps) => !existingIds.includes(donation.donationId)
            )
            // 기존 기부처 + 추가된 기부처
            return [...prev, ...uniqueNewDonations]
          })
        }
        setTotalPages(totalPages)
      }
      catch(error) {
        console.log("기부처 검색 실패", error)
      }
    }
    fetchDonations()
  },[decodedKeyword, pageNum])

  // 더보기를 클릭했을 때
  const handleLoadMore = () => {
    if(pageNum < totalPages) {
      setPageNum((prev) => prev + 1)
    }
  }

  return(
    <div className="relative flex flex-col w-full px-6 py-6">

      {/* 검색 모달창 */}
      {isSearchOpen && <SearchModal onClose={() => setIsSearchOpen(false)} /> }

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

      {/* 만약 빈 검색창이라면 */}
      {decodedKeyword.trim() === "" && (
        <div className="text-sm text-gray-500 mb-8 text-center">
          검색어를 입력해주세요!
        </div>
      )}

      {/* 결과 */}
      <div className="grid grid-cols-2 gap-4 pb-8">
        {donations.length > 0 ? (
          donations.map((donation) => (
            <DonationCard 
              key={donation.donationId}
              donationId={donation.donationId}
              donationName={donation.donationName}
            />
          ))
        ) : (
          <p className="col-span-2 flex justify-center items-center text-gray-500 text-sm">검색결과가 없습니다.</p>
        )}
      </div>

      {/* 더보기 버튼 */}
      {pageNum < totalPages && (
        <div className="flex justify-center items-center mt-4">
          <button onClick={handleLoadMore} className="flex flex-col items-center justify-center">
            <p className="text-gray-500">더보기</p>
            <img src={DownIcon} alt="더보기" className="w-6 h-6"/>
          </button>
        </div>
      )}

    </div>
  )
}