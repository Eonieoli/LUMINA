// 기부처 페이지에서 검색창을 누르면 보일 검색 모달창
import { useEffect, useState } from "react"
import DonateSearchBar from "../donate/SearchBar"
import BackIcon from '@/assets/images/donate/Ic_back.svg'
import { useNavigate } from "react-router-dom"
import { DonationProps } from "../donate/DonationCard"
import { getSearchDonations } from "@/apis/donation"

interface SearchModalProps {
  onClose: () => void
}

export default function SearchModal({onClose}: SearchModalProps){

  const [keyword, setKeyword] = useState("")
  const [imediateSearchResult, setImediateSearchResult] = useState<DonationProps[]>([])
  const navigate = useNavigate()

  // 검색어가 바뀔 때마다 api 호출하기
  useEffect(() => {
    const fetchSearchResults = async() => {
      const trimmedKeyword = keyword.trim()

      if(trimmedKeyword === "") {
        setImediateSearchResult([])
        return
      }
      
      try {
        const {donations}  = await getSearchDonations(trimmedKeyword, 1)
        setImediateSearchResult(donations)
        return
      }
      catch (error) {
        console.log("실시간 검색 중 에러 발생", error)
        setImediateSearchResult([])
      }
    }
    fetchSearchResults()
  },[keyword])

  // 돋보기 버튼을 클릭했을 때 기부처 검색 결과 페이지로 이동
  const handleSearchClick = () => {
    const finalkeyword = keyword.trim() === "" ? "donations" : encodeURIComponent(keyword)
    onClose() 
    navigate(`/donate/research/${finalkeyword}`)
  }

  // 뒤로가기 버튼 클릭했을 때 검색창 초기화하기
  const handleClose = () => {
    setKeyword("")
    onClose()
  }

  return (
    <div className="absolute top-0 left-0 w-full h-full z-40 px-6 py-6 bg-white">
      <div className="flex justify-start items-center mb-6 h-6">
        <img 
          src={BackIcon} 
          alt="BackIcon" 
          className="w-5 mr-2"
          onClick={handleClose}
        />
      </div>
      <DonateSearchBar 
        keyword={keyword}
        setKeyword={setKeyword}
        onSearchClick={handleSearchClick}
        autoFocus={true}
      />

      {/* 실시간 일치하는 기부처 렌더링 */}
      <ul className="mt-4 space-y-2" >
        {imediateSearchResult.map((donation) => (
          <li
            key={donation.donationId}  
            className="text-[16px] text-gray-600 cursor-pointer hover:bg-gray-100 px-2 py-2 rounded"
            onClick={() => {
              onClose()
              // 상세페이지로 이동하도록 추후 수정 예정
              navigate(`/donate/${donation.donationId}`)
            }} 
          >
            {donation.donationName}
          </li>
        ))}
      </ul>
    </div>
  )
}